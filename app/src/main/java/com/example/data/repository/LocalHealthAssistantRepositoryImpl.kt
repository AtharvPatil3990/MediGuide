package com.example.data.repository

import com.example.core.localization.AppLanguage
import com.example.data.local.entity.ReminderStatus
import com.example.domain.repository.CareModuleRepository
import com.example.domain.repository.HealthAssistantRepository
import com.example.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first rule-based Health Assistant repository.
 * Inspects local Room state (schedules, refills, care modules) and responds in
 * English, Hindi, or Marathi. Designed behind [HealthAssistantRepository] so a
 * future network LLM / on-device model can be swapped in seamlessly.
 */
@Singleton
class LocalHealthAssistantRepositoryImpl @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val careModuleRepository: CareModuleRepository
) : HealthAssistantRepository {

    override suspend fun generateReply(
        userId: Long,
        query: String,
        language: AppLanguage
    ): String {
        val normalized = query.lowercase(Locale.ROOT)
        val medicines = medicationRepository.observeMedicinesForUser(userId).first()
        val todayReminders = medicationRepository.observeTodayReminders(userId).first()
        val lowStock = medicines.filter { it.isLowStock }
        val motherModule = careModuleRepository.observeMotherBabyModule(userId).first()
        val appointments = careModuleRepository.observeElderAppointments(userId).first()

        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return when {
            containsAny(normalized, "today", "dose", "schedule", "आज", "दवा", "औषध", "डोस") -> {
                if (todayReminders.isEmpty()) {
                    when (language) {
                        AppLanguage.ENGLISH -> "You have no scheduled doses left for today. Open the Medicines tab to add a medicine and set exact local alarms."
                        AppLanguage.HINDI -> "आज के लिए कोई निर्धारित खुराक नहीं है। सटीक ऑफ़लाइन अलार्म सेट करने के लिए 'दवाइयाँ' टैब से दवा जोड़ें।"
                        AppLanguage.MARATHI -> "आजसाठी कोणताही नियोजित डोस नाही. अचूक ऑफलाइन अलार्म लावण्यासाठी 'औषधे' टॅबमधून औषध जोडा."
                    }
                } else {
                    val summary = todayReminders.joinToString("\n") { r ->
                        val state = when (r.status) {
                            ReminderStatus.COMPLETED -> r.responseType?.name ?: "COMPLETED"
                            else -> r.status.name
                        }
                        "• ${r.medicineName} (${r.dosage}) at ${timeFmt.format(Date(r.scheduledTimeMillis))} — [$state]"
                    }
                    when (language) {
                        AppLanguage.ENGLISH -> "Here is today's offline dose schedule:\n$summary"
                        AppLanguage.HINDI -> "आज का आपका ऑफ़लाइन खुराक कार्यक्रम:\n$summary"
                        AppLanguage.MARATHI -> "आजचे तुमचे ऑफलाइन डोस वेळापत्रक:\n$summary"
                    }
                }
            }

            containsAny(normalized, "refill", "stock", "low", "रीफिल", "स्टॉक", "साठा") -> {
                if (lowStock.isEmpty()) {
                    when (language) {
                        AppLanguage.ENGLISH -> "All ${medicines.size} active medicines have healthy stock above their refill thresholds."
                        AppLanguage.HINDI -> "सभी ${medicines.size} सक्रिय दवाइयों का स्टॉक पर्याप्त है।"
                        AppLanguage.MARATHI -> "सर्व ${medicines.size} सक्रिय औषधांचा साठा पुरेसा आहे."
                    }
                } else {
                    val list = lowStock.joinToString(", ") { "${it.name} (${it.inventoryCount} left)" }
                    when (language) {
                        AppLanguage.ENGLISH -> "Refill alert for ${lowStock.size} medicine(s): $list. Tap '+10 Refill' in the Medicines tab after restocking."
                        AppLanguage.HINDI -> "${lowStock.size} दवाइयों का स्टॉक कम है: $list। कृपया जल्द रीफिल करें।"
                        AppLanguage.MARATHI -> "${lowStock.size} औषधांचा साठा कमी आहे: $list. कृपया लवकर साठा भरा."
                    }
                }
            }

            containsAny(normalized, "miss", "skip", "forgot", "छूट", "चुक") -> {
                when (language) {
                    AppLanguage.ENGLISH -> "Missed-dose rule of thumb: Take the dose as soon as you remember unless it is almost time for your next scheduled dose. Never double a dose without a doctor's advice. Mark skipped doses in Today's Lifecycle to keep adherence logs accurate."
                    AppLanguage.HINDI -> "छूटी हुई खुराक का नियम: याद आते ही खुराक लें, लेकिन यदि अगली खुराक का समय बहुत पास है तो दोहरी खुराक न लें। सटीक रिकॉर्ड के लिए 'छोड़ें' चुनें और डॉक्टर से सलाह लें।"
                    AppLanguage.MARATHI -> "चुकलेल्या डोससाठी नियम: लक्षात येताच डोस घ्या, पण पुढील डोसची वेळ जवळ आली असेल तर दुप्पट डोस घेऊ नका. अचूक नोंदीसाठी 'वगळा' निवडा आणि डॉक्टरांचा सल्ला घ्या."
                }
            }

            containsAny(normalized, "care", "mother", "baby", "elder", "pregnan", "देखभाल", "माँ", "बुजुर्ग", "काळजी", "माता", "ज्येष्ठ") -> {
                val week = motherModule?.pregnancyWeek ?: 24
                val upcomingAppts = appointments.count { !it.completed }
                when (language) {
                    AppLanguage.ENGLISH -> "Care Summary: Mother & Baby tracker is at Week $week (Blood Group ${motherModule?.bloodGroup ?: "O+"}). Elder Care has $upcomingAppts upcoming doctor appointment(s) and ${medicines.size} tracked medicines."
                    AppLanguage.HINDI -> "देखभाल सारांश: माँ और शिशु ट्रैकर सप्ताह $week पर है। बुजुर्ग देखभाल में $upcomingAppts आगामी अपॉइंटमेंट और ${medicines.size} सक्रिय दवाइयाँ दर्ज हैं।"
                    AppLanguage.MARATHI -> "काळजी सारांश: माता आणि बाळ ट्रॅकर आठवडा $week वर आहे. ज्येष्ठ नागरिक विभागात $upcomingAppts आगामी भेटी आणि ${medicines.size} सक्रिय औषधे नोंदवली आहेत."
                }
            }

            else -> {
                when (language) {
                    AppLanguage.ENGLISH -> "I'm your offline MediGuide assistant. Ask me about 'today's doses', 'refill check', 'missed dose guidance', or 'care summary' — all computed privately from your local Room vault."
                    AppLanguage.HINDI -> "मैं आपका ऑफ़लाइन मेडीगाइड सहायक हूँ। मुझसे 'आज की दवाइयाँ', 'रीफिल जांच', 'छूटी खुराक', या 'देखभाल सारांश' के बारे में पूछें।"
                    AppLanguage.MARATHI -> "मी तुमचा ऑफलाइन मेडीगाइड सहाय्यक आहे. मला 'आजची औषधे', 'साठा तपासणी', 'चुकलेला डोस', किंवा 'काळजी सारांश' बद्दल विचारा."
                }
            }
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean =
        keywords.any { text.contains(it) }
}

package com.example.playback

import androidx.compose.ui.graphics.Color

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class SheetBeat(
    val timestampMs: Long,
    val chordName: String = "",
    val notes: List<Int> = emptyList(), // Midi-like octave offset tones to draw on staff: e.g. 0 to 12
    val instruction: String = "" // e.g. "Arpegio suave", "Redoble", "Crescendo"
)

object SyncedLyricsAndMusicSheet {

    fun getLyricsForTrack(trackId: String): List<LyricLine> {
        return when (trackId) {
            "demo_lamento_boliviano" -> listOf(
                LyricLine(0L, "🎵 [Intro Instrumental de Guitarra] 🎵"),
                LyricLine(12000L, "Me quieren agitar..."),
                LyricLine(16000L, "Me incitan a gritar..."),
                LyricLine(20000L, "Soy como una roca, palabras no me tocan..."),
                LyricLine(24000L, "Adentro hay un volcán que pronto va a estallar..."),
                LyricLine(28000L, "Yo quiero estar libre..."),
                LyricLine(32000L, "Mucho más de lo que soy..."),
                LyricLine(36000L, "Pero como un perro, estoy atado a un error..."),
                LyricLine(42000L, "¡Y mi amor de ayer se va, se va!"),
                LyricLine(48000L, "Ehh ohh, lamento boliviano..."),
                LyricLine(54000L, "Que un día empezó... y no va a terminar..."),
                LyricLine(60000L, "Y yo estoy aquí, borracho y loco..."),
                LyricLine(66000L, "Y mi corazón idiota, siempre brillará..."),
                LyricLine(72000L, "Y yo estoy aquí, borracho y loco..."),
                LyricLine(78000L, "Y mi corazón idiota, siempre brillará..."),
                LyricLine(84000L, "🎵 [Solo de Clarinete Sintetizado] 🎵"),
                LyricLine(100000L, "No te peines en la cama..."),
                LyricLine(104000L, "Que los viajantes se van a atrasar..."),
                LyricLine(108000L, "Y yo estoy aquí, borracho y loco..."),
                LyricLine(114000L, "Y mi corazón idiota, siempre brillará..."),
                LyricLine(120000L, "¡Nos vemos en el compás del rock! 🎸")
            )
            "demo_musica_ligera" -> listOf(
                LyricLine(0L, "🎸 [Riff Inolvidable de Guitarra] 🎸"),
                LyricLine(8000L, "Ella durmió al calor de las masas..."),
                LyricLine(14000L, "Y yo desperté queriendo soñarla..."),
                LyricLine(20000L, "Algún tiempo atrás pensé en escribirle..."),
                LyricLine(26000L, "Y nunca busqué las cosas sencillas..."),
                LyricLine(32000L, "Ella usó mi cabeza como una almohada..."),
                LyricLine(38000L, "Y yo me senté en la arena de su cama..."),
                LyricLine(44000L, "¡No estaré de más! No dejes que acabe..."),
                LyricLine(51000L, "De aquel amor de música ligera..."),
                LyricLine(57000L, "Nada nos libra... nada más queda..."),
                LyricLine(65000L, "🎸 [Solo Magistral de Guitarra] 🎸"),
                LyricLine(80000L, "Nada más queda... ¡GRACIAS TOTALES! 🤘")
            )
            "demo_amanecer_carmesi" -> listOf(
                LyricLine(0L, "🎹 [Sintetizadores Atmosféricos] 🎹"),
                LyricLine(6000L, "Amanece en el cielo carmesí..."),
                LyricLine(12000L, "Siento el pulso rojo latir en mí..."),
                LyricLine(18000L, "Silencios de metal que gritan amor..."),
                LyricLine(24000L, "AP Organization trae esta canción..."),
                LyricLine(30000L, "Es la sinfonía de la pasión..."),
                LyricLine(36000L, "¡Amanece hoy, brilla esta ilusión!✨"),
                LyricLine(42000L, "🎹 [Arpegios Carmesí de Teclados] 🎹")
            )
            else -> listOf(
                LyricLine(0L, "🎶 Escuchando audio de alta fidelidad 🎶"),
                LyricLine(5000L, "Reproductor de la AP Organization"),
                LyricLine(10000L, "Ecualización activa de alta resolución"),
                LyricLine(15000L, "Reproduciendo sin anuncios publicitarios")
            )
        }
    }

    fun getSheetMusicForTrack(trackId: String): List<SheetBeat> {
        return when (trackId) {
            "demo_lamento_boliviano" -> listOf(
                SheetBeat(0L, "Em", listOf(4, 7, 11), "Rasgueo inicial (Bajo de Em)"),
                SheetBeat(4000L, "Am", listOf(9, 0, 4), "Línea rítmica amortiguada"),
                SheetBeat(8000L, "B7", listOf(11, 2, 6, 9), "Preparación con tensión"),
                SheetBeat(12000L, "Em", listOf(4, 7, 11), "Acompañamiento acústico"),
                SheetBeat(16000L, "Am", listOf(9, 0, 4), "Verso 1: Progresión constante"),
                SheetBeat(20000L, "D", listOf(2, 6, 9), "Sube la intensidad"),
                SheetBeat(24000L, "G", listOf(7, 11, 2), "Resolución temporal"),
                SheetBeat(28000L, "B7", listOf(11, 2, 6), "Tensión lírica"),
                SheetBeat(32000L, "Em", listOf(4, 7, 11), "Estribillo principal"),
                SheetBeat(36000L, "Am", listOf(9, 0, 4), "Cambio menor"),
                SheetBeat(42000L, "C", listOf(0, 4, 7), "Ascenso coral"),
                SheetBeat(48000L, "D", listOf(2, 6, 9), "Ehh ohh..."),
                SheetBeat(54000L, "Em", listOf(4, 7, 11), "Estribillo de lamento"),
                SheetBeat(60000L, "G", listOf(7, 11, 2), "¡Borracho y loco!"),
                SheetBeat(66000L, "D", listOf(2, 6, 9), "Corazón idiota"),
                SheetBeat(72000L, "Em", listOf(4, 7, 11), "Progresión álgida"),
                SheetBeat(78000L, "C", listOf(0, 4, 7), "Brillará, brillará")
            )
            "demo_musica_ligera" -> listOf(
                SheetBeat(0L, "Bm", listOf(11, 2, 6), "Riff principal: Acorde 1"),
                SheetBeat(2000L, "G", listOf(7, 11, 2), "Riff principal: Acorde 2"),
                SheetBeat(4000L, "D", listOf(2, 6, 9), "Riff principal: Acorde 3"),
                SheetBeat(6000L, "A", listOf(9, 1, 4), "Riff principal: Acorde 4"),
                SheetBeat(8000L, "Bm", listOf(11, 2, 6), "Entrada de voz - Ritmo marcado"),
                SheetBeat(14000L, "G", listOf(7, 11, 2), "Verso: Mantener tensión"),
                SheetBeat(20000L, "D", listOf(2, 6, 9), "Cambio rápido de trastes"),
                SheetBeat(26000L, "A", listOf(9, 1, 4), "Preparación coro"),
                SheetBeat(32000L, "Bm", listOf(11, 2, 6), "Almohada de masas"),
                SheetBeat(51000L, "G", listOf(7, 11, 2), "Música Ligera coro"),
                SheetBeat(57000L, "A", listOf(9, 1, 4), "Nada más queda")
            )
            "demo_amanecer_carmesi" -> listOf(
                SheetBeat(0L, "Am", listOf(9, 0, 4), "Arpegio de teclas"),
                SheetBeat(3000L, "F", listOf(5, 9, 0), "Sintetizador de bajos"),
                SheetBeat(6000L, "C", listOf(0, 4, 7), "Melodía principal de violín"),
                SheetBeat(9000L, "G", listOf(7, 11, 2), "Amanecer carmesí..."),
                SheetBeat(12000L, "Dm", listOf(2, 5, 9), "Bajos profundos estilo AP"),
                SheetBeat(18000L, "Am", listOf(9, 0, 4), "Resolución armónica nocturna")
            )
            else -> listOf(
                SheetBeat(0L, "C", listOf(0, 4, 7), "Normal (AP Std) / Acorde Base"),
                SheetBeat(5000L, "F", listOf(5, 9, 0), "Ajustes de frecuencia activos"),
                SheetBeat(10000L, "G", listOf(7, 11, 2), "Compás balanceado"),
                SheetBeat(15000L, "Am", listOf(9, 0, 4), "Acorde de resolución")
            )
        }
    }
}

package com.example

object KorgKromeSoundBank {

    fun getBankLetter(lsb: Int): String {
        return when (lsb) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            16 -> "User-A"
            17 -> "User-B"
            18 -> "User-C"
            19 -> "User-D"
            20 -> "User-E"
            21 -> "User-F"
            else -> "B$lsb"
        }
    }

    fun getPatchCode(lsb: Int, program: Int): String {
        val bankLetter = getBankLetter(lsb)
        val pNum = String.format("%03d", program.coerceIn(0, 127))
        return "$bankLetter$pNum"
    }

    fun getSoundName(msb: Int, lsb: Int, program: Int, mode: String, customName: String? = null): String {
        if (!customName.isNullOrBlank()) {
            val patchCode = getPatchCode(lsb, program)
            if (patchCode.isNotEmpty() && customName.contains(patchCode)) {
                return customName
            }
            return if (patchCode.isNotEmpty()) "$patchCode: $customName" else customName
        }
        val patchCode = getPatchCode(lsb, program)
        val isCombi = mode.equals("Combi", ignoreCase = true)
        val prefix = if (isCombi) "COMBI" else "PROG"
        val key = "${prefix}_${patchCode}"

        val knownName = soundMap[key]
        if (knownName != null) {
            return "$patchCode: $knownName"
        }

        if (msb == 121) {
            val gmName = gmSoundMap[program.coerceIn(0, 127)] ?: "GM Sound ${program + 1}"
            return "GM${String.format("%03d", program)}: $gmName"
        }

        // Default formatting if exact patch name is not in preset dictionary
        val modeText = if (isCombi) "Combi" else "Prog"
        return "$modeText $patchCode (Bank $lsb, PC $program)"
    }

    private val gmSoundMap = mapOf(
        0 to "Acoustic Grand Piano", 1 to "Bright Acoustic Piano", 2 to "Electric Grand Piano", 3 to "Honky-tonk Piano",
        4 to "Electric Piano 1", 5 to "Electric Piano 2", 6 to "Harpsichord", 7 to "Clavinet",
        8 to "Celesta", 9 to "Glockenspiel", 10 to "Music Box", 11 to "Vibraphone",
        12 to "Marimba", 13 to "Xylophone", 14 to "Tubular Bells", 15 to "Dulcimer",
        16 to "Drawbar Organ", 17 to "Percussive Organ", 18 to "Rock Organ", 19 to "Church Organ",
        20 to "Reed Organ", 21 to "Accordion", 22 to "Harmonica", 23 to "Tango Accordion",
        24 to "Acoustic Guitar (nylon)", 25 to "Acoustic Guitar (steel)", 26 to "Electric Guitar (jazz)", 27 to "Electric Guitar (clean)",
        28 to "Electric Guitar (muted)", 29 to "Overdriven Guitar", 30 to "Distortion Guitar", 31 to "Guitar harmonics",
        32 to "Acoustic Bass", 33 to "Electric Bass (finger)", 34 to "Electric Bass (pick)", 35 to "Fretless Bass",
        36 to "Slap Bass 1", 37 to "Slap Bass 2", 38 to "Synth Bass 1", 39 to "Synth Bass 2",
        40 to "Violin", 41 to "Viola", 42 to "Cello", 43 to "Contrabass",
        44 to "Tremolo Strings", 45 to "Pizzicato Strings", 46 to "Orchestral Harp", 47 to "Timpani",
        48 to "String Ensemble 1", 49 to "String Ensemble 2", 50 to "Synth Strings 1", 51 to "Synth Strings 2",
        52 to "Choir Aahs", 53 to "Voice Oohs", 54 to "Synth Voice", 55 to "Orchestra Hit",
        56 to "Trumpet", 57 to "Trombone", 58 to "Tuba", 59 to "Muted Trumpet",
        60 to "French Horn", 61 to "Brass Section", 62 to "Synth Brass 1", 63 to "Synth Brass 2",
        64 to "Soprano Sax", 65 to "Alto Sax", 66 to "Tenor Sax", 67 to "Baritone Sax",
        68 to "Oboe", 69 to "English Horn", 70 to "Bassoon", 71 to "Clarinet",
        72 to "Piccolo", 73 to "Flute", 74 to "Recorder", 75 to "Pan Flute",
        76 to "Blown Bottle", 77 to "Shakuhachi", 78 to "Whistle", 79 to "Ocarina",
        80 to "Lead 1 (square)", 81 to "Lead 2 (sawtooth)", 82 to "Lead 3 (calliope)", 83 to "Lead 4 (chiff)",
        84 to "Lead 5 (charang)", 85 to "Lead 6 (voice)", 86 to "Lead 7 (fifths)", 87 to "Lead 8 (bass + lead)",
        88 to "Pad 1 (new age)", 89 to "Pad 2 (warm)", 90 to "Pad 3 (polysynth)", 91 to "Pad 4 (choir)",
        92 to "Pad 5 (bowed)", 93 to "Pad 6 (metallic)", 94 to "Pad 7 (halo)", 95 to "Pad 8 (sweep)"
    )

    private val soundMap = mapOf(
        // KORG KROME PROG BANK A
        "PROG_A000" to "German Monolithic Grand",
        "PROG_A001" to "German Dark Grand",
        "PROG_A002" to "Japanese Bright Grand",
        "PROG_A003" to "Upright Piano",
        "PROG_A004" to "M1 Piano",
        "PROG_A005" to "Krome Stereo EP",
        "PROG_A006" to "Mark I Stage EP",
        "PROG_A007" to "Dyno EP Custom",
        "PROG_A008" to "Suitcase EP 1973",
        "PROG_A009" to "Wurlitzer EP 200A",
        "PROG_A010" to "FM Tine EP",
        "PROG_A011" to "Clavinet D6 Mute",
        "PROG_A012" to "CX-3 B3 Jazz Organ",
        "PROG_A013" to "Rock Organ Percussion",
        "PROG_A014" to "Gospel Full Pipes",
        "PROG_A015" to "Accordion Musette",
        "PROG_A016" to "Acoustic Steel Guitar",
        "PROG_A017" to "Nylon String Guitar",
        "PROG_A018" to "Clean Strat Electric",
        "PROG_A019" to "Overdrive Lead Guitar",
        "PROG_A020" to "Distortion Lead Guitar",
        "PROG_A021" to "Acoustic Master Bass",
        "PROG_A022" to "Finger Bass Jazz",
        "PROG_A023" to "Slap Bass Mark II",
        "PROG_A024" to "Synth Bass Saw",
        "PROG_A025" to "Stereo Strings Session",
        "PROG_A026" to "Chamber Bowed Strings",
        "PROG_A027" to "Pizzicato Section",
        "PROG_A028" to "Pop Brass Section",
        "PROG_A029" to "Solo Trumpet Vibrato",
        "PROG_A030" to "Tenor Sax Expression",
        "PROG_A031" to "Alto Sax Lead",
        "PROG_A032" to "Flute Concert C",
        "PROG_A033" to "Analog Synth Lead",
        "PROG_A034" to "Warm Pad Sweep",
        "PROG_A035" to "Motion Synth Pad",

        // KORG KROME PROG BANK B
        "PROG_B000" to "Studio Grand Piano",
        "PROG_B001" to "Rock Piano Bright",
        "PROG_B002" to "Honky-Tonk Piano",
        "PROG_B003" to "Vintage Tine EP",
        "PROG_B004" to "Reed EP Tremolo",
        "PROG_B005" to "Harpsichord KeyOff",
        "PROG_B006" to "Full Drawbars Organ",
        "PROG_B007" to "Church Pipe Organ",
        "PROG_B008" to "12-String Acoustic",
        "PROG_B009" to "Jazz Guitar Archtop",
        "PROG_B010" to "Funk Metal Guitar",
        "PROG_B011" to "Precision Bass",
        "PROG_B012" to "Mini Moog Synth Bass",
        "PROG_B013" to "Symphonic Strings",
        "PROG_B014" to "Orchestral Harp",
        "PROG_B015" to "French Horn Ensemble",
        "PROG_B016" to "Soprano Sax",
        "PROG_B017" to "Clarinet Lead",
        "PROG_B018" to "Sawtooth Lead Synth",
        "PROG_B019" to "Square Wave Lead",
        "PROG_B020" to "Heavenly Ambient Pad",

        // KORG KROME PROG BANK C
        "PROG_C000" to "Grand Piano & Strings",
        "PROG_C001" to "Layered Dyno EP",
        "PROG_C002" to "Percussive Organ B3",
        "PROG_C003" to "Flamenco Guitar",
        "PROG_C004" to "Muted Electric Guitar",
        "PROG_C005" to "Fretless Bass",
        "PROG_C006" to "Acid Synth Bass",
        "PROG_C007" to "Hollywood Strings",
        "PROG_C008" to "Brass Fanfare",
        "PROG_C009" to "Baritone Sax",
        "PROG_C010" to "Pan Flute",
        "PROG_C011" to "Polysynth Pad",

        // KORG KROME PROG BANK D
        "PROG_D000" to "Dance Piano Cut",
        "PROG_D001" to "CP80 Electric Grand",
        "PROG_D002" to "Phase EP",
        "PROG_D003" to "Rotary Organ",
        "PROG_D004" to "Distortion Rhythm",
        "PROG_D005" to "Reso Synth Bass",
        "PROG_D006" to "Cinema Pad",

        // KORG KROME COMBI BANK A
        "COMBI_A000" to "Piano & Vocal Pad",
        "COMBI_A001" to "Krome Studio Piano & Strings",
        "COMBI_A002" to "Dyno EP & Analog Pad",
        "COMBI_A003" to "Rock Horns & Rhythm",
        "COMBI_A004" to "Orchestral Suite",
        "COMBI_A005" to "Split Bass & Lead Organ",
        "COMBI_A006" to "Acoustic Guitar & Flute Layer",
        "COMBI_A007" to "Modern Pop Performance",
        "COMBI_A008" to "Synth Layer Lead & Pad",
        "COMBI_A009" to "EDM Festival Layer",

        // KORG KROME COMBI BANK B
        "COMBI_B000" to "Stage Piano & Full Strings",
        "COMBI_B001" to "Tine EP & Warm Sweep",
        "COMBI_B002" to "Jazz Trio Split",
        "COMBI_B003" to "Symphonic Brass & Strings",
        "COMBI_B004" to "Ambient Motion Soundscape",

        // KORG KROME COMBI BANK C
        "COMBI_C000" to "Grand Piano & Cello",
        "COMBI_C001" to "Wurlitzer & Tremolo Pad",
        "COMBI_C002" to "Latin Salsa Band Layer",
        "COMBI_C003" to "Electro Dance Split"
    )
}

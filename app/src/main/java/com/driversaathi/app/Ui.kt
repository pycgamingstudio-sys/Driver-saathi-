package com.driversaathi.app

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------ design tokens

object DS {
    val bg = Color(0xFFF6F7FB)
    val surface = Color.White
    val surface2 = Color(0xFFEEF1FA)
    val line = Color(0xFFE2E6F2)
    val text = Color(0xFF0F1B3D)
    val muted = Color(0xFF6B7694)
    val accent = Color(0xFF3D5AFE)
    val accent2 = Color(0xFF7C4DFF)
    val good = Color(0xFF10B981)
    val warn = Color(0xFFD97706)
    val grad: Brush = Brush.linearGradient(listOf(accent, accent2))
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = DS.accent,
            onPrimary = Color.White,
            background = DS.bg,
            surface = DS.surface,
            onBackground = DS.text,
            onSurface = DS.text
        ),
        content = content
    )
}

// ------------------------------------------------------------------ small building blocks

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color(0x333D5AFE), spotColor = Color(0x333D5AFE))
            .clip(shape)
            .background(DS.surface)
            .border(1.dp, DS.line, shape),
        content = content
    )
}

@Composable
fun H1(text: String) =
    Text(text, color = DS.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)

@Composable
fun Muted(text: String, size: Int = 14) =
    Text(text, color = DS.muted, fontSize = size.sp)

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(if (enabled) DS.grad else SolidColor(DS.surface2))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) Color.White else DS.muted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .height(46.dp)
            .clip(shape)
            .background(DS.surface2)
            .border(1.dp, DS.line, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = DS.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun Bubble(text: String, mine: Boolean, meta: String? = null) {
    val shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (mine) 18.dp else 4.dp,
        bottomEnd = if (mine) 4.dp else 18.dp
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(if (mine) DS.grad else SolidColor(DS.surface2))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (meta != null) Text(meta, color = if (mine) Color(0xCCFFFFFF) else DS.muted, fontSize = 12.sp)
            Text(text, color = if (mine) Color.White else DS.text, fontSize = 15.sp)
        }
    }
}

// ------------------------------------------------------------------ root

@Composable
fun Root(act: MainActivity) {
    val tick = Store.tick
    var tab by remember { mutableIntStateOf(0) }

    if (!Store.consent(act)) {
        ConsentScreen { Store.setConsent(act) }
        return
    }

    Scaffold(
        containerColor = DS.bg,
        bottomBar = {
            NavigationBar(containerColor = DS.surface) {
                listOf("घर", "तैयारी", "संदेश").forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Text(listOf("🏠", "⚙️", "💬")[i], fontSize = 20.sp) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = DS.text,
                            unselectedTextColor = DS.muted,
                            indicatorColor = DS.surface2
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(act, tick) { tab = 1 }
                1 -> SetupScreen(act, tick)
                else -> MessagesScreen(act, tick)
            }
        }
    }
}

// ------------------------------------------------------------------ consent

@Composable
fun ConsentScreen(onAgree: () -> Unit) {
    var checked by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().background(DS.bg).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(DS.grad),
            contentAlignment = Alignment.Center
        ) { Text("🛡️", fontSize = 32.sp) }
        H1("आपकी सहमति")
        Muted("शुरू करने से पहले ये तीन बातें जान लीजिए", 15)

        Panel {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ConsentRow("✍️", "ऐप सिर्फ़ आपके बोलने पर चैट बॉक्स में मैसेज लिखेगा।")
                ConsentRow("👆", "Send, Accept और Reject का बटन हमेशा आप ही दबाएँगे।")
                ConsentRow("🎙️", "आपकी आवाज़ रिकॉर्ड या सेव नहीं होती।")
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = DS.accent,
                    uncheckedColor = DS.muted,
                    checkmarkColor = Color.White
                )
            )
            Text("मैं सहमत हूँ", color = DS.text)
        }
        PrimaryButton("आगे बढ़ें", enabled = checked, onClick = onAgree)
    }
}

@Composable
private fun ConsentRow(icon: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 20.sp)
        Text(text, color = DS.text, fontSize = 15.sp)
    }
}

// ------------------------------------------------------------------ home

@Composable
fun HomeScreen(act: MainActivity, tick: Int, goSetup: () -> Unit) {
    val online = Store.online(act)
    val last = Store.messages(act).firstOrNull()
    val ready = Perms.notifAccess(act)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                H1("नमस्ते 👋")
                Muted("आज की सवारी आराम से")
            }
            StatusPill(online)
        }

        // voice orb
        VoiceOrb(online = online, onTap = { act.listen() })

        // duty switch
        Panel {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (online) "ड्यूटी चालू" else "ड्यूटी बंद", color = DS.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Muted(if (online) "आने वाले मैसेज बोलकर सुनाऊँगा" else "चालू करने पर मैसेज बोलकर सुनाऊँगा")
                }
                Switch(
                    checked = online,
                    onCheckedChange = { Store.setOnline(act, it) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = DS.good,
                        checkedThumbColor = Color.White,
                        checkedBorderColor = DS.good,
                        uncheckedTrackColor = DS.surface2,
                        uncheckedThumbColor = DS.muted,
                        uncheckedBorderColor = DS.line
                    )
                )
            }
        }

        if (!ready) {
            Panel {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("⚠️ सेटअप अधूरा है", color = DS.warn, fontWeight = FontWeight.Bold)
                        Muted("मैसेज पढ़ने की अनुमति चाहिए")
                    }
                    GhostButton("पूरा करें", Modifier.widthIn(min = 110.dp), goSetup)
                }
            }
        }

        // last conversation
        Panel {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Muted("आख़िरी मैसेज")
                if (last == null) {
                    Text("अभी कोई मैसेज नहीं आया।", color = DS.text)
                } else {
                    val body = if (last.title.isBlank()) last.text else "${last.title}\n${last.text}"
                    Bubble(body, mine = false, meta = last.platform)
                    if (last.draft.isNotBlank()) Bubble(last.draft, mine = true, meta = "आपका ड्राफ़्ट")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GhostButton("🔊 दोबारा सुनो", Modifier.weight(1f)) {
                            Speaker.speak("${last.platform}। ${last.title}। ${last.text}")
                        }
                        if (last.draft.isNotBlank()) {
                            GhostButton("📋 कॉपी", Modifier.weight(1f)) { act.copy(last.draft) }
                        }
                    }
                }
            }
        }

        // launch ride apps
        Muted("ऐप खोलें")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("ola" to "Ola", "uber" to "Uber", "rapido" to "Rapido").forEach { (key, label) ->
                GhostButton(label, Modifier.weight(1f)) { act.openRide(key) }
            }
        }

        Muted("Send हमेशा आप दबाते हैं।", 13)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatusPill(online: Boolean) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape).background(DS.surface).border(1.dp, DS.line, shape).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(if (online) DS.good else DS.muted))
        Text(if (online) "ऑनलाइन" else "ऑफ़लाइन", color = DS.text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun VoiceOrb(online: Boolean, onTap: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "orb")
    val s by inf.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "scale"
    )
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            if (online) {
                Box(
                    Modifier
                        .size(170.dp)
                        .graphicsLayer {
                            scaleX = s
                            scaleY = s
                            alpha = ((1.35f - s) / 0.35f).coerceIn(0f, 1f) * 0.5f
                        }
                        .clip(CircleShape)
                        .background(DS.accent)
                )
            }
            Box(
                Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(if (online) DS.grad else SolidColor(DS.surface2))
                    .clickable(onClick = onTap),
                contentAlignment = Alignment.Center
            ) { Text("🎤", fontSize = 48.sp) }
        }
        Text("दबाकर बोलिए", color = DS.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Muted(if (online) "जवाब बोलिए, मैं ड्राफ़्ट लिख दूँगा" else "ड्यूटी चालू करके मैसेज सुनिए")
    }
}

// ------------------------------------------------------------------ setup

@Composable
fun SetupRow(icon: String, title: String, desc: String, ok: Boolean, onFix: () -> Unit) {
    Panel {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(DS.surface2), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 20.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = DS.text, fontWeight = FontWeight.Bold)
                Muted(desc, 13)
            }
            if (ok) Text("✅", fontSize = 22.sp)
            else GhostButton("ठीक करें", Modifier.widthIn(min = 92.dp), onFix)
        }
    }
}

@Composable
fun SetupScreen(act: MainActivity, tick: Int) {
    val n = Perms.notifAccess(act)
    val m = Perms.mic(act)
    val a = Perms.a11y(act)
    val b = Perms.battery(act)
    val done = listOf(n, m, a, b).count { it }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        H1("ऐप तैयार करें")
        Muted("4 में से $done पूरे", 15)
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(DS.surface2)) {
            Box(Modifier.fillMaxWidth(done / 4f).fillMaxHeight().clip(CircleShape).background(DS.grad))
        }
        Spacer(Modifier.height(4.dp))

        SetupRow("🔔", "नोटिफ़िकेशन पढ़ने की अनुमति", "ग्राहक के मैसेज पढ़ने के लिए", n) {
            act.open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        SetupRow("🎙️", "माइक", "आपकी आवाज़ सुनने के लिए", m) { act.requestMic() }
        SetupRow("✍️", "चैट बॉक्स में ड्राफ़्ट लिखना", "सिर्फ़ ड्राफ़्ट भरता है, Send नहीं दबाता", a) {
            act.open(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }
        SetupRow("🔋", "बैटरी बंद न करे", "ऐप चालू रहे इसलिए", b) { act.openBattery() }

        Panel {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("अगर सेटिंग में बटन ग्रे है", color = DS.text, fontWeight = FontWeight.Bold)
                Muted("फ़ोन सेटिंग > ऐप्स > Driver Saathi > ऊपर ⋮ > \"Restricted settings\" को Allow करें। फिर दोबारा कोशिश करें।", 13)
            }
        }

        Panel {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("टेस्ट मोड", color = DS.text, fontWeight = FontWeight.Bold)
                    Muted("किसी भी ऐप की नोटिफ़िकेशन पढ़ो (जाँच के लिए)", 13)
                }
                Switch(
                    checked = Store.testMode(act),
                    onCheckedChange = { Store.setTestMode(act, it) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = DS.accent,
                        checkedThumbColor = Color.White,
                        checkedBorderColor = DS.accent,
                        uncheckedTrackColor = DS.surface2,
                        uncheckedThumbColor = DS.muted,
                        uncheckedBorderColor = DS.line
                    )
                )
            }
        }

        PrimaryButton("🔊 आवाज़ टेस्ट करें") {
            Speaker.speak("नमस्ते, मैं ड्राइवर साथी हूँ। मेरी आवाज़ सुनाई दे रही है?")
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ------------------------------------------------------------------ messages

@Composable
fun MessagesScreen(act: MainActivity, tick: Int) {
    val list = Store.messages(act)
    val fmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        H1("संदेश")
        Spacer(Modifier.height(12.dp))
        if (list.isEmpty()) {
            Panel {
                Column(
                    Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💬", fontSize = 36.sp)
                    Text("अभी कोई मैसेज नहीं आया।", color = DS.text, fontWeight = FontWeight.Bold)
                    Muted("ग्राहक का मैसेज आते ही यहाँ दिखेगा")
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(list) { m ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val body = if (m.title.isBlank()) m.text else "${m.title}\n${m.text}"
                        Bubble(body, mine = false, meta = "${m.platform} • ${fmt.format(Date(m.time))}")
                        if (m.draft.isNotBlank()) Bubble(m.draft, mine = true, meta = "आपका ड्राफ़्ट")
                    }
                }
            }
        }
    }
}

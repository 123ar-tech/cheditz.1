package com.cheditz.admin.data.model

import org.json.JSONArray
import org.json.JSONObject

fun SiteConfig.toJson(): String {
    val o = JSONObject()
    o.put("accent", accent)
    o.put("works", JSONArray().also { arr ->
        works.forEach { w ->
            arr.put(JSONObject().apply {
                put("id", w.id)
                put("title", w.title)
                put("sub", w.sub)
                put("cat", w.cat)
                put("meta", w.meta)
                put("type", w.type)
                put("url", w.url)
                put("visible", w.visible)
            })
        }
    })
    o.put("content", JSONObject().apply {
        put("heroBadge", content.heroBadge)
        put("heroHead", content.heroHead)
        put("heroSub", content.heroSub)
        put("worksTitle", content.worksTitle)
        put("svcTitle", content.svcTitle)
        put("orderTitle", content.orderTitle)
        put("orderDesc", content.orderDesc)
        put("contactHead", content.contactHead)
        put("contactDesc", content.contactDesc)
    })
    o.put("services", JSONArray().also { arr ->
        services.forEach { s ->
            arr.put(JSONObject().apply {
                put("name", s.name)
                put("desc", s.desc)
                put("feats", s.feats)
                put("price", s.price)
            })
        }
    })
    o.put("contact", JSONObject().apply {
        put("email", contact.email)
        put("insta", contact.insta)
        put("instaUrl", contact.instaUrl)
        put("whatsapp", contact.whatsapp)
        put("whatsappLabel", contact.whatsappLabel)
        put("whatsappText", contact.whatsappText)
        put("hours", contact.hours)
        put("reply", contact.reply)
    })
    o.put("testimonials", JSONArray().also { arr ->
        testimonials.forEach { t ->
            arr.put(JSONObject().apply {
                put("name", t.name)
                put("role", t.role)
                put("rating", t.rating)
                put("text", t.text)
                put("date", t.date)
            })
        }
    })
    return o.toString(2)
}

fun siteConfigFromJson(raw: String): SiteConfig {
    val o = JSONObject(raw)
    val contentO = o.optJSONObject("content") ?: JSONObject()
    val contactO = o.optJSONObject("contact") ?: JSONObject()
    return SiteConfig(
        accent = o.optString("accent", "#bc6bff"),
        works = o.optJSONArray("works").let { arr ->
            if (arr == null) emptyList() else (0 until arr.length()).map { i ->
                val w = arr.getJSONObject(i)
                WorkItem(
                    id = w.optString("id", "w-import-$i${System.currentTimeMillis()}"),
                    title = w.optString("title"),
                    sub = w.optString("sub"),
                    cat = w.optString("cat"),
                    meta = w.optString("meta"),
                    type = w.optString("type", "image"),
                    url = w.optString("url"),
                    visible = w.optBoolean("visible", true)
                )
            }
        },
        content = ContentBlock(
            heroBadge = contentO.optString("heroBadge"),
            heroHead = contentO.optString("heroHead"),
            heroSub = contentO.optString("heroSub"),
            worksTitle = contentO.optString("worksTitle"),
            svcTitle = contentO.optString("svcTitle"),
            orderTitle = contentO.optString("orderTitle"),
            orderDesc = contentO.optString("orderDesc"),
            contactHead = contentO.optString("contactHead"),
            contactDesc = contentO.optString("contactDesc")
        ),
        services = o.optJSONArray("services").let { arr ->
            if (arr == null) emptyList() else (0 until arr.length()).map { i ->
                val s = arr.getJSONObject(i)
                Service(
                    name = s.optString("name"),
                    desc = s.optString("desc"),
                    feats = s.optString("feats"),
                    price = s.optString("price")
                )
            }
        },
        contact = Contact(
            email = contactO.optString("email"),
            insta = contactO.optString("insta"),
            instaUrl = contactO.optString("instaUrl"),
            whatsapp = contactO.optString("whatsapp"),
            whatsappLabel = contactO.optString("whatsappLabel"),
            whatsappText = contactO.optString("whatsappText"),
            hours = contactO.optString("hours"),
            reply = contactO.optString("reply")
        ),
        testimonials = o.optJSONArray("testimonials").let { arr ->
            if (arr == null) emptyList() else (0 until arr.length()).map { i ->
                val t = arr.getJSONObject(i)
                Testimonial(
                    id = t.optString("id"),
                    name = t.optString("name"),
                    role = t.optString("role"),
                    rating = t.optInt("rating", 5),
                    text = t.optString("text"),
                    date = t.optString("date")
                )
            }
        }
    )
}

fun defaultSiteConfig(): SiteConfig = SiteConfig(
    accent = "#9333ea",
    works = listOf(
        WorkItem(
            id = "d1", title = "Vande Bharat", sub = "Nagercoil - Chennai", cat = "Poster",
            meta = "Rail launch - A2", type = "image",
            url = "https://miaoda-conversation-file.s3cdn.medo.dev/user-c4hvi7diyakg/app-c4hvyv74mdxd/20260605/Vande Bharat Nagercoil to Chennai Poster.png",
            visible = true
        ),
        WorkItem(
            id = "d2", title = "Video Reel", sub = "Brand edit", cat = "Video",
            meta = "Short-form", type = "video",
            url = "https://drive.google.com/uc?export=download&id=15yMqmznY3JufjA20k1vnS0Cz9c8T8UqN",
            visible = true
        )
    ),
    content = ContentBlock(
        heroBadge = "Open for commissions . 2026",
        heroHead = "<em>Edits that linger long after the scroll.</em>",
        heroSub = "Cheditz is a one-desk editing house for photo, video, and document work. Sharpen frames, colour the mood, polish words - quietly, quickly, on deadline.",
        worksTitle = "Recent commissions.",
        svcTitle = "Three crafts.One steady hand.",
        orderTitle = "Tell us whatyou need.",
        orderDesc = "Fill the brief below and we'll reply with a quote and timeline within hours. For files over 25 MB, drop a WeTransfer or Drive link in the notes.",
        contactHead = "Let's makesomethingunforgettable.",
        contactDesc = "Quick questions, partnership ideas, or just to say hi or to place order - the inbox is always open."
    ),
    services = listOf(
        Service(
            name = "Poster Editing",
            desc = "Event flyers, movie-style posters, product launches, social posts, and brand campaign artwork - designed and finished print-ready.",
            feats = "Event & function posters\nMovie / cinematic style\nProduct & sale flyers\nSocial posts & stories",
            price = "₹20 / poster"
        ),
        Service(
            name = "Video Editing",
            desc = "Reels, shorts, long-form cuts, sound design, motion titles, and end-to-end colour grading for narrative and brand pieces.",
            feats = "Instagram Reels & YouTube Shorts\nLong-form narrative cuts\nMotion titles & lower thirds\nFull colour grading",
            price = "₹20 / minute"
        ),
        Service(
            name = "Document Editing",
            desc = "Proofreading, copy editing, layout in InDesign/Word, formatting for theses, ebooks, pitch decks, and reports.",
            feats = "Proofreading & line editing\nThesis & manuscript formatting\nPitch decks & one-pagers\nEbook layout (EPUB / PDF)",
            price = "₹20 / word"
        )
    ),
    contact = Contact(
        email = "cheditz94@gmail.com",
        insta = "@cheditz1",
        instaUrl = "https://www.instagram.com/cheditz1?igsh=bXcyYXgzZ2l2Nm8w",
        whatsapp = "",
        whatsappLabel = "",
        whatsappText = "",
        hours = "Mon - Sat, 9:00 - 19:00 IST",
        reply = "Replies usually within 4 hours."
    ),
    testimonials = emptyList()
)

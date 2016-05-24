package com.komanov.serialization.converters

import java.time.Instant
import java.util.UUID

import com.komanov.serialization.domain._

object TestData {

  val baseDate = Instant.parse("2016-05-24T10:00:00Z")
  val zeroGuid = new UUID(0, 0)

  val site1k = Site(
    UUID.fromString("cafedead-1a9c-4be0-92ef-7da630bb0010"),
    UUID.fromString("cafebabe-1a9c-4be0-92ef-7da630bb0001"),
    20160524L,
    SiteType.Flash,
    Seq(SiteFlag.Free),
    "some-site-name",
    "This is a long long description about this site, maybe something important here, maybe not.\n" +
      "Ещё давайте добавим немного русского языка, чтобы не только английский был.\n" +
      "And a little bit English, just to add some bytes to output.",
    Nil,
    Seq(MetaTag("og:image", "http://example.org/title.png")),
    Seq(
      Page(
        "index",
        "/",
        Nil,
        Seq(
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0011"),
            PageComponentType.Text,
            TextComponentData("Title"),
            None,
            baseDate.plusSeconds(86400),
            baseDate.plusSeconds(86400)
          )
        )
      )
    ),
    Seq(
      FreeEntryPoint("user", "some-site-name", primary = true)
    ),
    published = true,
    baseDate,
    baseDate.plusSeconds(86400)
  )

  val site2k = Site(
    UUID.fromString("cafedead-1a9c-4be0-92ef-7da630bb0020"),
    UUID.fromString("cafebabe-1a9c-4be0-92ef-7da630bb0002"),
    20160523L,
    SiteType.Silverlight,
    Seq(SiteFlag.Premium),
    "another-site-name",
    "This is a long long description about this site, maybe something important here, maybe not.\n" +
      "Ещё давайте добавим немного русского языка, чтобы не только английский был.\n" +
      "And a little bit English, just to add some bytes to output.\n" +
      "This is a long long description about this site, maybe something important here, maybe not.\n" +
      "Ещё давайте добавим немного русского языка, чтобы не только английский был.\n" +
      "And a little bit English, just to add some bytes to output.",
    Seq(
      Domain("example.com", primary = true),
      Domain("example.org", primary = false)
    ),
    Seq(
      MetaTag("og:image", "http://example.org/title.png"),
      MetaTag("og:image1", "http://example.org/title1.png"),
      MetaTag("og:image2", "http://example.org/title2.png"),
      MetaTag("og:image3", "http://example.org/title3.png"),
      MetaTag("og:image4", "http://example.org/title4.png")
    ),
    Seq(
      Page(
        "index",
        "/",
        Nil,
        Seq(
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0021"),
            PageComponentType.Text,
            TextComponentData("Another Title"),
            None,
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0022"),
            PageComponentType.Button,
            ButtonComponentData("click_btn", "Click Me!", UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0023")),
            Some(PageComponentPosition(1, 1)),
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          )
        )
      )
    ),
    Seq(
      DomainEntryPoint("example.com", primary = true),
      DomainEntryPoint("example.org", primary = false),
      FreeEntryPoint("user", "another-site-name", primary = false)
    ),
    published = true,
    baseDate,
    baseDate.plusSeconds(86412)
  )

  val site4k = Site(
    UUID.fromString("cafedead-1a9c-4be0-92ef-7da630bb0030"),
    UUID.fromString("cafebabe-1a9c-4be0-92ef-7da630bb0003"),
    20160523L,
    SiteType.Silverlight,
    Seq(SiteFlag.Premium),
    "another-site-name-2",
    "This is a long long description about this site, maybe something important here, maybe not.\n" +
      "Ещё давайте добавим немного русского языка, чтобы не только английский был.\n" +
      "And a little bit English, just to add some bytes to output.\n" +
      "This is a long long description about this site, maybe something important here, maybe not.\n" +
      "Ещё давайте добавим немного русского языка, чтобы не только английский был.\n" +
      "This is a long long description about this site, maybe something important here, maybe not.\n" +
      "Ещё давайте добавим немного русского языка, чтобы не только английский был.\n" +
      "And a little bit English, just to add some bytes to output.",
    Seq(
      Domain("example4k.com", primary = true),
      Domain("example4k.org", primary = false)
    ),
    Seq(
      MetaTag("og:image", "http://example4k.org/title.png"),
      MetaTag("og:image1", "http://example4k.org/title1.png"),
      MetaTag("og:image2", "http://example4k.org/title2.png"),
      MetaTag("og:image3", "http://example4k.org/title3.png"),
      MetaTag("og:image4", "http://example4k.org/title4.png"),
      MetaTag("og:title", "Title for Facebook"),
      MetaTag("og:description", "Description for Facebook")
    ),
    Seq(
      Page(
        "index",
        "/",
        Nil,
        Seq(
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0031"),
            PageComponentType.Text,
            TextComponentData("Another Title"),
            None,
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0032"),
            PageComponentType.Button,
            ButtonComponentData("click_btn", "Click Me!", UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0033")),
            Some(PageComponentPosition(1, 1)),
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          )
        )
      ),
      Page(
        "blog",
        "/blog",
        Seq(
          MetaTag("og:type", "blog")
        ),
        Seq(
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0034"),
            PageComponentType.Text,
            TextComponentData("My blog!"),
            None,
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0035"),
            PageComponentType.Blog,
            BlogComponentData("BLOG", rss = true, tags = true),
            None,
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0036"),
            PageComponentType.Text,
            TextComponentData("Some blog entry, a little bit text, a little bit not text."),
            Some(PageComponentPosition(0, 100)),
            baseDate.plusSeconds(1),
            baseDate.plusSeconds(1)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0037"),
            PageComponentType.Button,
            ButtonComponentData("post_comment", "Comment!", UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0038")),
            Some(PageComponentPosition(0, 0)),
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0039"),
            PageComponentType.Text,
            TextComponentData("My blog!"),
            Some(PageComponentPosition(0, 100)),
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          ),
          PageComponent(
            UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0029"),
            PageComponentType.Button,
            ButtonComponentData("post_comment", "Comment!", UUID.fromString("cafedead-1a9c-4be0-c0c0-7da630bb0038")),
            Some(PageComponentPosition(0, 0)),
            baseDate.plusSeconds(86401),
            baseDate.plusSeconds(86402)
          )
        )
      )
    ),
    Seq(
      DomainEntryPoint("example4k.com", primary = true),
      DomainEntryPoint("example4k.org", primary = false),
      DomainEntryPoint("subdomain1.example4k.org", primary = false),
      DomainEntryPoint("subdomain2.example4k.org", primary = false),
      DomainEntryPoint("subdomain3.example4k.org", primary = false),
      FreeEntryPoint("user", "another-site-name-2", primary = false)
    ),
    published = true,
    baseDate,
    baseDate.plusSeconds(86413)
  )

  val site8k = site4k.copy(
    siteType = SiteType.Html5,
    pages = site4k.pages ++ (0 to 7).map { index =>
      Page(
        s"catalog$index",
        s"/ct/$index",
        Nil,
        Seq(
          PageComponent(
            UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}031"),
            PageComponentType.Text,
            TextComponentData(s"Catalog $index"),
            None,
            baseDate.plusSeconds(86501),
            baseDate.plusSeconds(86502)
          ),
          PageComponent(
            UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}032"),
            PageComponentType.Button,
            ButtonComponentData("click", "Order", UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}033")),
            Some(PageComponentPosition(1, 1)),
            baseDate.plusSeconds(86601),
            baseDate.plusSeconds(86602)
          )
        )
      )
    }
  )

  val site64k = site4k.copy(
    siteType = SiteType.Html5,
    pages = site4k.pages ++ (10 to 49).map { index =>
      Page(
        s"catalog$index",
        s"/product/catalog/$index",
        (0 to 9).map(tagIndex => MetaTag(s"name$tagIndex", s"value-of-a-tag-$tagIndex")),
        Seq(
          PageComponent(
            UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}31"),
            PageComponentType.Text,
            TextComponentData(s"Product catalog title $index"),
            None,
            baseDate.plusSeconds(86501),
            baseDate.plusSeconds(86502)
          ),
          PageComponent(
            UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}32"),
            PageComponentType.Button,
            ButtonComponentData("click", "Order", UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}33")),
            Some(PageComponentPosition(100, 500)),
            baseDate.plusSeconds(86601),
            baseDate.plusSeconds(86602)
          ),
          PageComponent(
            UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}34"),
            PageComponentType.Text,
            TextComponentData(s"Product catalog title $index"),
            None,
            baseDate.plusSeconds(86501),
            baseDate.plusSeconds(86502)
          ),
          PageComponent(
            UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}035"),
            PageComponentType.Button,
            ButtonComponentData("click", "Order", UUID.fromString(s"cafedead-1a9c-4be0-c0c0-7da630bb${index}36")),
            Some(PageComponentPosition(100, 1000)),
            baseDate.plusSeconds(86601),
            baseDate.plusSeconds(86602)
          )
        )
      )
    },
    entryPoints = site4k.entryPoints ++ (0 to 19).map { index =>
      DomainEntryPoint(s"mirror$index.example.org", primary = false)
    },
    defaultMetaTags = (1 to 20).map { index =>
      MetaTag(s"og:special-tag-$index", s"our special value $index")
    }
  )

  val all: Seq[(String, Site)] = Seq(
    "1k" -> site1k,
    "2k" -> site2k,
    "4k" -> site4k,
    "8k" -> site8k,
    "64k" -> site64k
  )

}

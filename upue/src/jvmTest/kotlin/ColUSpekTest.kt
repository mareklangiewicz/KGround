package pl.mareklangiewicz.upue

import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.*


internal infix fun <T> T.eq(expected: T) = check(this == expected) { "$this != $expected" }

class ColUSpekTest {

    @TestFactory
    fun uspekTest() = uspekTestFactory {
        "On big nr strings data source lst" o {
            val srclst = MutLst<String>()
            for (i in 1000 until 1300) srclst.tail.push(buildString {
                append(i)
            })

            "On imap with reversed values" o {
                val stdMap = srclst.associateWith { it.reversed() }.toMutableMap()
                val imap = stdMap.asMutMap()

                "check 1200" o { imap["1200"] eq "0021" }

                "On mutate 1200" o {
                    imap["1200"] = "bla"

                    "check mutated 1200" o { imap["1200"] eq "bla" }
                    "check source also mutated" o { stdMap["1200"] eq "bla" }
                }

                "On mutate stdMap source 1250" o {
                    stdMap["1250"] = "ble"

                    "check source mutated 1250" o { stdMap["1250"] eq "ble" }
                    "check imap also mutated 1250" o { imap["1250"] eq "ble" }
                }

                "On imap.cut(11)" o {
                    val cutRO = imap.cut("11")

                    "check cutted keys len" o { cutRO.keys.len eq 100 }
                    "check cutted stuff" o {
                        for (i in 1000 until 1300) {
                            val imapkey = i.toString()
                            val icutkey = imapkey.removePrefix("11")
                            val expected = if (i !in 1100 until 1200) null else imapkey.reversed()
                            cutRO[icutkey] eq expected
                        }
                    }
                    "check src imap still unchanged" o {
                        for (i in 1000 until 1300) {
                            val imapkey = i.toString()
                            imap[imapkey] eq imapkey.reversed()
                        }
                    }

                    "On mutate src outside cutted stuff" o {
                        imap["1099"] = "xxx"

                        "check cutted stuff same as before" o {
                            for (i in 1000 until 1300) {
                                val imapkey = i.toString()
                                val icutkey = imapkey.removePrefix("11")
                                val expected = if (i !in 1100 until 1200) null else imapkey.reversed()
                                cutRO[icutkey] eq expected
                            }
                        }
                    }

                    "On mutate src inside cutted stuff" o {
                        imap["1199"] = "xxx"

                        "check cutted stuff same as before" o {
                            for (i in 1000 until 1300) {
                                val imapkey = i.toString()
                                val icutkey = imapkey.removePrefix("11")
                                val expected = when (i) {
                                    1199 -> "xxx"
                                    !in 1100 until 1200 -> null
                                    else -> imapkey.reversed()
                                }
                                cutRO[icutkey] eq expected
                            }
                        }
                    }
                }

                "On imap.cutMut(11)" o {
                    val cutRW = imap.cutMut("11")

                    "check cutted keys len" o { cutRW.keys.len eq 100 }
                    "check cutted stuff" o {
                        for (i in 1000 until 1300) {
                            val imapkey = i.toString()
                            val icutkey = imapkey.removePrefix("11")
                            val expected = if (i !in 1100 until 1200) null else imapkey.reversed()
                            cutRW[icutkey] eq expected
                        }
                    }
                    "check src imap still unchanged" o {
                        for (i in 1000 until 1300) {
                            val imapkey = i.toString()
                            imap[imapkey] eq imapkey.reversed()
                        }
                    }

                    "On mutate src outside cutted stuff" o {
                        imap["1099"] = "xxx"

                        "check cutted stuff same as before" o {
                            for (i in 1000 until 1300) {
                                val imapkey = i.toString()
                                val icutkey = imapkey.removePrefix("11")
                                val expected = if (i !in 1100 until 1200) null else imapkey.reversed()
                                cutRW[icutkey] eq expected
                            }
                        }
                    }

                    "On mutate src inside cutted stuff" o {
                        imap["1199"] = "xxx"

                        "check cutted stuff same as before" o {
                            for (i in 1000 until 1300) {
                                val imapkey = i.toString()
                                val icutkey = imapkey.removePrefix("11")
                                val expected = when (i) {
                                    1199 -> "xxx"
                                    !in 1100 until 1200 -> null
                                    else -> imapkey.reversed()
                                }
                                cutRW[icutkey] eq expected
                            }
                        }
                    }

                    "On muttate cutted stuff" o {
                        cutRW["22"] = "yyy" // it is key "1122" in src imap

                        "check cutted stuff changed correctly" o {
                            for (i in 1000 until 1300) {
                                val imapkey = i.toString()
                                val icutkey = imapkey.removePrefix("11")
                                val expected = when (i) {
                                    1122 -> "yyy"
                                    !in 1100 until 1200 -> null
                                    else -> imapkey.reversed()
                                }
                                cutRW[icutkey] eq expected
                            }
                        }

                        "check src imap also changed correctly" o {
                            for (i in 1000 until 1300) {
                                val imapkey = i.toString()
                                val expected = when (i) {
                                    1122 -> "yyy"
                                    else -> imapkey.reversed()
                                }
                                imap[imapkey] eq expected
                            }
                        }
                    }
                }
            }
        }
    }
}

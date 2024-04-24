package pl.mareklangiewicz.kgroundx.io

import pl.mareklangiewicz.kground.tee

// TODO_later:
// Some dummy code is needed in kotlinx module, so we don't have issues with maven publications.
// Track this issue:
// https://youtrack.jetbrains.com/issue/KT-52344/Unclear-error-for-task-generateMetadataFileForIosArm64Publication-when-the-sources-are-empty
// And maybe someday remove this code when fixed.
// UPDATE: Or maybe remove this whole module??

fun dummyKGroundXIOTee() = "dummy kgroundx".tee

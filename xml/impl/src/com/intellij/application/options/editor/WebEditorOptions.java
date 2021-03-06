/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.application.options.editor;

import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.xml.XmlBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author spleaner
 */
@State(
  name="XmlEditorOptions",
  storages= {
    @Storage(
      file = StoragePathMacros.APP_CONFIG + "/editor.xml"
    )}
)
public class WebEditorOptions implements PersistentStateComponent<WebEditorOptions>, ExportableComponent {

  private boolean myBreadcrumbsEnabled = true;
  private boolean myBreadcrumbsEnabledInXml = false;
  private boolean myShowCssColorPreviewInGutter = true;
  private boolean mySelectWholeCssSelectorSuffixOnDoubleClick = true;
  private boolean myShowCssInlineColorPreview = false;
  private boolean myAutomaticallyInsertClosingTag = true;
  private boolean myAutomaticallyInsertRequiredAttributes = true;
  private boolean myAutomaticallyInsertRequiredSubTags = true;
  private boolean myAutomaticallyStartAttribute = true;
  private boolean myZenCodingEnabled = true;
  private int myZenCodingExpandShortcut = TemplateSettings.TAB_CHAR;

  private boolean myTagTreeHighlightingEnabled = true;
  private int myTagTreeHighlightingLevelCount = 6;
  private int myTagTreeHighlightingOpacity = 10;

  public static WebEditorOptions getInstance() {
    return ServiceManager.getService(WebEditorOptions.class);
  }

  public WebEditorOptions() {
    setTagTreeHighlightingEnabled(!ApplicationManager.getApplication().isUnitTestMode());
  }

  public void setBreadcrumbsEnabled(boolean b) {
    myBreadcrumbsEnabled = b;
  }

  public boolean isBreadcrumbsEnabled() {
    return myBreadcrumbsEnabled;
  }

  public void setBreadcrumbsEnabledInXml(boolean b) {
    myBreadcrumbsEnabledInXml = b;
  }

  public boolean isBreadcrumbsEnabledInXml() {
    return myBreadcrumbsEnabledInXml;
  }

  public boolean isShowCssInlineColorPreview() {
    return myShowCssInlineColorPreview;
  }

  public void setShowCssInlineColorPreview(final boolean showCssInlineColorPreview) {
    myShowCssInlineColorPreview = showCssInlineColorPreview;
  }

  public boolean isShowCssColorPreviewInGutter() {
    return myShowCssColorPreviewInGutter;
  }

  public void setShowCssColorPreviewInGutter(final boolean showCssColorPreviewInGutter) {
    myShowCssColorPreviewInGutter = showCssColorPreviewInGutter;
  }

  public boolean isAutomaticallyInsertClosingTag() {
    return myAutomaticallyInsertClosingTag;
  }

  public void setAutomaticallyInsertClosingTag(final boolean automaticallyInsertClosingTag) {
    myAutomaticallyInsertClosingTag = automaticallyInsertClosingTag;
  }

  public boolean isAutomaticallyInsertRequiredAttributes() {
    return myAutomaticallyInsertRequiredAttributes;
  }

  public void setAutomaticallyInsertRequiredAttributes(final boolean automaticallyInsertRequiredAttributes) {
    myAutomaticallyInsertRequiredAttributes = automaticallyInsertRequiredAttributes;
  }

  /**
   * @deprecated use {@link com.intellij.application.options.emmet.EmmetOptions#getEmmetExpandShortcut()}
   */
  @Deprecated
  public int getZenCodingExpandShortcut() {
    return myZenCodingExpandShortcut;
  }

  public boolean isAutomaticallyStartAttribute() {
    return myAutomaticallyStartAttribute;
  }

  /**
   * @deprecated use {@link com.intellij.application.options.emmet.EmmetOptions#isEmmetEnabled()}
   */
  @Deprecated
  public boolean isZenCodingEnabled() {
    return myZenCodingEnabled;
  }

  public void setAutomaticallyStartAttribute(final boolean automaticallyStartAttribute) {
    myAutomaticallyStartAttribute = automaticallyStartAttribute;
  }

  public boolean isAutomaticallyInsertRequiredSubTags() {
    return myAutomaticallyInsertRequiredSubTags;
  }

  public void setAutomaticallyInsertRequiredSubTags(boolean automaticallyInsertRequiredSubTags) {
    myAutomaticallyInsertRequiredSubTags = automaticallyInsertRequiredSubTags;
  }

  public void setTagTreeHighlightingLevelCount(int tagTreeHighlightingLevelCount) {
    myTagTreeHighlightingLevelCount = tagTreeHighlightingLevelCount;
  }

  public int getTagTreeHighlightingLevelCount() {
    return myTagTreeHighlightingLevelCount;
  }

  public void setTagTreeHighlightingOpacity(int tagTreeHighlightingOpacity) {
    myTagTreeHighlightingOpacity = tagTreeHighlightingOpacity;
  }

  public int getTagTreeHighlightingOpacity() {
    return myTagTreeHighlightingOpacity;
  }

  public void setTagTreeHighlightingEnabled(boolean tagTreeHighlightingEnabled) {
    myTagTreeHighlightingEnabled = tagTreeHighlightingEnabled;
  }

  public boolean isTagTreeHighlightingEnabled() {
    return myTagTreeHighlightingEnabled;
  }

  @NotNull
  public File[] getExportFiles() {
    return new File[]{PathManager.getOptionsFile("editor")};
  }

  @NotNull
  public String getPresentableName() {
    return XmlBundle.message("xml.options");
  }

  @Nullable
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public WebEditorOptions getState() {
    return this;
  }

  public void loadState(final WebEditorOptions state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean isSelectWholeCssSelectorSuffixOnDoubleClick() {
    return mySelectWholeCssSelectorSuffixOnDoubleClick;
  }

  public void setSelectWholeCssSelectorSuffixOnDoubleClick(boolean selectWholeCssSelectorSuffixOnDoubleClick) {
    mySelectWholeCssSelectorSuffixOnDoubleClick = selectWholeCssSelectorSuffixOnDoubleClick;
  }
}

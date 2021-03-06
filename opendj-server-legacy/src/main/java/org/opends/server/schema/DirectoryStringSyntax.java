/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2006-2008 Sun Microsystems, Inc.
 *      Portions Copyright 2012-2015 ForgeRock AS
 */
package org.opends.server.schema;
import static org.opends.server.schema.SchemaConstants.*;

import java.util.List;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.opendj.config.server.ConfigChangeResult;
import org.forgerock.opendj.config.server.ConfigException;
import org.forgerock.opendj.ldap.schema.Schema;
import org.forgerock.opendj.ldap.schema.SchemaOptions;
import org.forgerock.opendj.ldap.schema.Syntax;
import org.forgerock.util.Option;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.DirectoryStringAttributeSyntaxCfg;
import org.opends.server.api.AttributeSyntax;
import org.opends.server.core.ServerContext;


/**
 * This class defines the directory string attribute syntax, which is simply a
 * set of UTF-8 characters.  By default, they will be treated in a
 * case-insensitive manner, and equality, ordering, substring, and approximate
 * matching will be allowed.
 */
public class DirectoryStringSyntax
       extends AttributeSyntax<DirectoryStringAttributeSyntaxCfg>
       implements ConfigurationChangeListener<DirectoryStringAttributeSyntaxCfg>
{

  /** Indicates whether we will allow zero-length values. */
  private boolean allowZeroLengthValues;

  /** The reference to the configuration for this directory string syntax. */
  private DirectoryStringAttributeSyntaxCfg currentConfig;

  private ServerContext serverContext;

  /**
   * Creates a new instance of this syntax.  Note that the only thing that
   * should be done here is to invoke the default constructor for the
   * superclass.  All initialization should be performed in the
   * <CODE>initializeSyntax</CODE> method.
   */
  public DirectoryStringSyntax()
  {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public void initializeSyntax(DirectoryStringAttributeSyntaxCfg configuration, ServerContext serverContext)
         throws ConfigException
  {
    this.serverContext = serverContext;

    // This syntax is one of the Directory Server's core syntaxes and therefore
    // it may be instantiated at times without a configuration entry.  If that
    // is the case, then we'll exit now before doing anything that could require
    // access to that entry.
    if (configuration == null)
    {
      return;
    }

    currentConfig = configuration;
    currentConfig.addDirectoryStringChangeListener(this);
    allowZeroLengthValues = currentConfig.isAllowZeroLengthValues();
    updateNewSchema();
  }

  /** Update the option in new schema if it changes from current value. */
  private void updateNewSchema()
  {
    Option<Boolean> option = SchemaOptions.ALLOW_ZERO_LENGTH_DIRECTORY_STRINGS;
    if (allowZeroLengthValues != serverContext.getSchemaNG().getOption(option))
    {
      SchemaUpdater schemaUpdater = serverContext.getSchemaUpdater();
      schemaUpdater.updateSchema(
          schemaUpdater.getSchemaBuilder().setOption(option, allowZeroLengthValues).toSchema());
    }
  }

  /** {@inheritDoc} */
  @Override
  public Syntax getSDKSyntax(Schema schema)
  {
    return schema.getSyntax(SchemaConstants.SYNTAX_DIRECTORY_STRING_OID);
  }

  /**
   * Performs any finalization that may be necessary for this attribute syntax.
   */
  @Override
  public void finalizeSyntax()
  {
    currentConfig.removeDirectoryStringChangeListener(this);
  }

  /**
   * Retrieves the common name for this attribute syntax.
   *
   * @return  The common name for this attribute syntax.
   */
  @Override
  public String getName()
  {
    return SYNTAX_DIRECTORY_STRING_NAME;
  }

  /**
   * Retrieves the OID for this attribute syntax.
   *
   * @return  The OID for this attribute syntax.
   */
  @Override
  public String getOID()
  {
    return SYNTAX_DIRECTORY_STRING_OID;
  }

  /**
   * Retrieves a description for this attribute syntax.
   *
   * @return  A description for this attribute syntax.
   */
  @Override
  public String getDescription()
  {
    return SYNTAX_DIRECTORY_STRING_DESCRIPTION;
  }

  /**
   * Indicates whether zero-length values will be allowed.  This is technically
   * forbidden by the LDAP specification, but it was allowed in earlier versions
   * of the server, and the discussion of the directory string syntax in RFC
   * 2252 does not explicitly state that they are not allowed.
   *
   * @return  <CODE>true</CODE> if zero-length values should be allowed for
   *          attributes with a directory string syntax, or <CODE>false</CODE>
   *          if not.
   */
  public boolean allowZeroLengthValues()
  {
    return allowZeroLengthValues;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isConfigurationChangeAcceptable(
                      DirectoryStringAttributeSyntaxCfg configuration,
                      List<LocalizableMessage> unacceptableReasons)
  {
    // The configuration will always be acceptable.
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public ConfigChangeResult applyConfigurationChange(
              DirectoryStringAttributeSyntaxCfg configuration)
  {
    currentConfig = configuration;
    allowZeroLengthValues = configuration.isAllowZeroLengthValues();
    updateNewSchema();

    return new ConfigChangeResult();
  }
}


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
 *      Copyright 2008-2009 Sun Microsystems, Inc.
 *      Portions Copyright 2014-2015 ForgeRock AS
 */
package org.opends.server.admin;

import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.i18n.LocalizableMessageBuilder;
import org.forgerock.opendj.config.DurationUnit;
import org.forgerock.opendj.config.SizeUnit;
import org.forgerock.util.Utils;

/**
 * A property definition visitor which can be used to generate syntax
 * usage information.
 */
public final class PropertyDefinitionUsageBuilder {

  /**
   * Underlying implementation.
   */
  private class MyPropertyDefinitionVisitor extends
      PropertyDefinitionVisitor<LocalizableMessage, Void> {

    /**
     * Flag indicating whether detailed syntax information will be
     * generated.
     */
    private final boolean isDetailed;

    /** The formatter to use for numeric values. */
    private final NumberFormat numberFormat;



    /** Private constructor. */
    private MyPropertyDefinitionVisitor(boolean isDetailed) {
      this.isDetailed = isDetailed;

      this.numberFormat = NumberFormat.getNumberInstance();
      this.numberFormat.setGroupingUsed(true);
      this.numberFormat.setMaximumFractionDigits(2);
    }



    /** {@inheritDoc} */
    @Override
    public <C extends ConfigurationClient, S extends Configuration>
    LocalizableMessage visitAggregation(AggregationPropertyDefinition<C, S> d, Void p) {
      return LocalizableMessage.raw("NAME");
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitAttributeType(AttributeTypePropertyDefinition d,
        Void p) {
      return LocalizableMessage.raw("OID");
    }

    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitACI(ACIPropertyDefinition d, Void p) {
      return LocalizableMessage.raw("ACI");
    }

    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitBoolean(BooleanPropertyDefinition d, Void p) {
      if (isDetailed) {
        return LocalizableMessage.raw("false | true");
      } else {
        return LocalizableMessage.raw("BOOLEAN");
      }
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitClass(ClassPropertyDefinition d, Void p) {
      if (isDetailed && !d.getInstanceOfInterface().isEmpty()) {
        return LocalizableMessage.raw("CLASS <= " + d.getInstanceOfInterface().get(0));
      } else {
        return LocalizableMessage.raw("CLASS");
      }
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitDN(DNPropertyDefinition d, Void p) {
      if (isDetailed && d.getBaseDN() != null) {
        return LocalizableMessage.raw("DN <= " + d.getBaseDN());
      } else {
        return LocalizableMessage.raw("DN");
      }
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitDuration(DurationPropertyDefinition d, Void p) {
      LocalizableMessageBuilder builder = new LocalizableMessageBuilder();
      DurationUnit unit = d.getBaseUnit();

      if (isDetailed && d.getLowerLimit() > 0) {
        builder.append(DurationUnit.toString(d.getLowerLimit()));
        builder.append(" <= ");
      }

      builder.append("DURATION (");
      builder.append(unit.getShortName());
      builder.append(")");

      if (isDetailed) {
        if (d.getUpperLimit() != null) {
          builder.append(" <= ");
          builder.append(DurationUnit.toString(d.getUpperLimit()));
        }

        if (d.isAllowUnlimited()) {
          builder.append(" | unlimited");
        }
      }

      return builder.toMessage();
    }



    /** {@inheritDoc} */
    @Override
    public <E extends Enum<E>> LocalizableMessage visitEnum(EnumPropertyDefinition<E> d,
        Void p) {
      if (!isDetailed) {
        // Use the last word in the property name.
        String name = d.getName();
        int i = name.lastIndexOf('-');
        if (i == -1 || i == (name.length() - 1)) {
          return LocalizableMessage.raw(name.toUpperCase());
        } else {
          return LocalizableMessage.raw(name.substring(i + 1).toUpperCase());
        }
      } else {
        Set<String> values = new TreeSet<>();
        for (Object value : EnumSet.allOf(d.getEnumClass())) {
          values.add(value.toString().trim().toLowerCase());
        }
        return LocalizableMessage.raw(Utils.joinAsString(" | ", values));
      }
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitInteger(IntegerPropertyDefinition d, Void p) {
      LocalizableMessageBuilder builder = new LocalizableMessageBuilder();

      if (isDetailed) {
        builder.append(String.valueOf(d.getLowerLimit()));
        builder.append(" <= ");
      }

      builder.append("INTEGER");

      if (isDetailed) {
        if (d.getUpperLimit() != null) {
          builder.append(" <= ");
          builder.append(String.valueOf(d.getUpperLimit()));
        } else if (d.isAllowUnlimited()) {
          builder.append(" | unlimited");
        }
      }

      return builder.toMessage();
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitIPAddress(IPAddressPropertyDefinition d, Void p) {
      return LocalizableMessage.raw("HOST_NAME");
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitIPAddressMask(IPAddressMaskPropertyDefinition d,
        Void p) {
      return LocalizableMessage.raw("IP_ADDRESS_MASK");
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitSize(SizePropertyDefinition d, Void p) {
      LocalizableMessageBuilder builder = new LocalizableMessageBuilder();

      if (isDetailed && d.getLowerLimit() > 0) {
        SizeUnit unit = SizeUnit.getBestFitUnitExact(d.getLowerLimit());
        builder.append(numberFormat.format(unit.fromBytes(d.getLowerLimit())));
        builder.append(' ');
        builder.append(unit.getShortName());
        builder.append(" <= ");
      }

      builder.append("SIZE");

      if (isDetailed) {
        if (d.getUpperLimit() != null) {
          long upperLimit = d.getUpperLimit();
          SizeUnit unit = SizeUnit.getBestFitUnitExact(upperLimit);

          // Quite often an upper limit is some power of 2 minus 1. In those
          // cases lets use a "less than" relation rather than a "less than
          // or equal to" relation. This will result in a much more readable
          // quantity.
          if (unit == SizeUnit.BYTES && upperLimit < Long.MAX_VALUE) {
            unit = SizeUnit.getBestFitUnitExact(upperLimit + 1);
            if (unit != SizeUnit.BYTES) {
              upperLimit += 1;
              builder.append(" < ");
            } else {
              builder.append(" <= ");
            }
          } else {
            builder.append(" <= ");
          }

          builder.append(numberFormat.format(unit.fromBytes(upperLimit)));
          builder.append(' ');
          builder.append(unit.getShortName());
        }

        if (d.isAllowUnlimited()) {
          builder.append(" | unlimited");
        }
      }

      return builder.toMessage();
    }



    /** {@inheritDoc} */
    @Override
    public LocalizableMessage visitString(StringPropertyDefinition d, Void p) {
      if (d.getPattern() != null) {
        if (isDetailed) {
          LocalizableMessageBuilder builder = new LocalizableMessageBuilder();
          builder.append(d.getPatternUsage());
          builder.append(" - ");
          final LocalizableMessage synopsis = d.getPatternSynopsis();
          if (synopsis != null)
          {
            builder.append(synopsis);
          }
          return builder.toMessage();
        } else {
          return LocalizableMessage.raw(d.getPatternUsage());
        }
      } else {
        return LocalizableMessage.raw("STRING");
      }
    }



    /** {@inheritDoc} */
    @Override
    public <T> LocalizableMessage visitUnknown(PropertyDefinition<T> d, Void p)
        throws PropertyException {
      return LocalizableMessage.raw("?");
    }
  }

  /** Underlying implementation. */
  private final MyPropertyDefinitionVisitor pimpl;



  /**
   * Creates a new property usage builder.
   *
   * @param isDetailed
   *          Indicates whether or not the generated usage should
   *          contain detailed information such as constraints.
   */
  public PropertyDefinitionUsageBuilder(boolean isDetailed) {
    this.pimpl = new MyPropertyDefinitionVisitor(isDetailed);
  }



  /**
   * Generates the usage information for the provided property
   * definition.
   *
   * @param pd
   *          The property definitions.
   * @return Returns the usage information for the provided property
   *         definition.
   */
  public LocalizableMessage getUsage(PropertyDefinition<?> pd) {
    return pd.accept(pimpl, null);
  }
}

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
 *      Copyright 2010 Sun Microsystems, Inc.
 *      Portions copyright 2011-2012 ForgeRock AS
 */

package org.forgerock.opendj.ldap.requests;

import com.forgerock.opendj.util.StaticUtils;

/**
 * Unmodifiable plain SASL bind request implementation.
 */
final class UnmodifiablePlainSASLBindRequestImpl extends
        AbstractUnmodifiableSASLBindRequest<PlainSASLBindRequest> implements PlainSASLBindRequest {
    UnmodifiablePlainSASLBindRequestImpl(PlainSASLBindRequest impl) {
        super(impl);
    }

    @Override
    public String getAuthenticationID() {
        return impl.getAuthenticationID();
    }

    @Override
    public String getAuthorizationID() {
        return impl.getAuthorizationID();
    }

    @Override
    public byte[] getPassword() {
        // Defensive copy.
        return StaticUtils.copyOfBytes(impl.getPassword());
    }

    @Override
    public PlainSASLBindRequest setAuthenticationID(String authenticationID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlainSASLBindRequest setAuthorizationID(String authorizationID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlainSASLBindRequest setPassword(byte[] password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlainSASLBindRequest setPassword(char[] password) {
        throw new UnsupportedOperationException();
    }
}

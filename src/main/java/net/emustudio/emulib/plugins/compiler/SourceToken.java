/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import java.util.Objects;

/** Immutable lexical token with optional compiler-provided semantic editing metadata. */
public final class SourceToken implements Token {
    private final int type, offset;
    private final String text, name, scope, referenceScope, referencedFile;

    public SourceToken(int type, int offset, String text, String name, String scope, String referenceScope) {
        this(type, offset, text, name, scope, referenceScope, null);
    }

    public SourceToken(int type, int offset, String text, String name, String scope, String referenceScope, String referencedFile) {
        this.type = type; this.offset = offset; this.text = Objects.requireNonNull(text);
        this.name = name; this.scope = scope; this.referenceScope = referenceScope;
        this.referencedFile = referencedFile;
    }
    @Override public int getType() { return type; }
    @Override public int getOffset() { return offset; }
    @Override public String getText() { return text; }
    @Override public String getSymbolName() { return name; }
    @Override public String getSymbolScope() { return scope; }
    @Override public String getReferenceScope() { return referenceScope; }
    @Override public String getReferencedFile() { return referencedFile; }
}

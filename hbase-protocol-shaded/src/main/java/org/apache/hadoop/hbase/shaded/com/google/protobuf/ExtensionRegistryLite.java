begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Protocol Buffers - Google's data interchange format
end_comment

begin_comment
comment|// Copyright 2008 Google Inc.  All rights reserved.
end_comment

begin_comment
comment|// https://developers.google.com/protocol-buffers/
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// Redistribution and use in source and binary forms, with or without
end_comment

begin_comment
comment|// modification, are permitted provided that the following conditions are
end_comment

begin_comment
comment|// met:
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|//     * Redistributions of source code must retain the above copyright
end_comment

begin_comment
comment|// notice, this list of conditions and the following disclaimer.
end_comment

begin_comment
comment|//     * Redistributions in binary form must reproduce the above
end_comment

begin_comment
comment|// copyright notice, this list of conditions and the following disclaimer
end_comment

begin_comment
comment|// in the documentation and/or other materials provided with the
end_comment

begin_comment
comment|// distribution.
end_comment

begin_comment
comment|//     * Neither the name of Google Inc. nor the names of its
end_comment

begin_comment
comment|// contributors may be used to endorse or promote products derived from
end_comment

begin_comment
comment|// this software without specific prior written permission.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
end_comment

begin_comment
comment|// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
end_comment

begin_comment
comment|// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
end_comment

begin_comment
comment|// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
end_comment

begin_comment
comment|// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
end_comment

begin_comment
comment|// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
end_comment

begin_comment
comment|// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
end_comment

begin_comment
comment|// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
end_comment

begin_comment
comment|// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
end_comment

begin_comment
comment|// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Equivalent to {@link ExtensionRegistry} but supports only "lite" types.  *<p>  * If all of your types are lite types, then you only need to use  * {@code ExtensionRegistryLite}.  Similarly, if all your types are regular  * types, then you only need {@link ExtensionRegistry}.  Typically it does not  * make sense to mix the two, since if you have any regular types in your  * program, you then require the full runtime and lose all the benefits of  * the lite runtime, so you might as well make all your types be regular types.  * However, in some cases (e.g. when depending on multiple third-party libraries  * where one uses lite types and one uses regular), you may find yourself  * wanting to mix the two.  In this case things get more complicated.  *<p>  * There are three factors to consider:  Whether the type being extended is  * lite, whether the embedded type (in the case of a message-typed extension)  * is lite, and whether the extension itself is lite.  Since all three are  * declared in different files, they could all be different.  Here are all  * the combinations and which type of registry to use:  *<pre>  *   Extended type     Inner type    Extension         Use registry  *   =======================================================================  *   lite              lite          lite              ExtensionRegistryLite  *   lite              regular       lite              ExtensionRegistry  *   regular           regular       regular           ExtensionRegistry  *   all other combinations                            not supported  *</pre>  *<p>  * Note that just as regular types are not allowed to contain lite-type fields,  * they are also not allowed to contain lite-type extensions.  This is because  * regular types must be fully accessible via reflection, which in turn means  * that all the inner messages must also support reflection.  On the other hand,  * since regular types implement the entire lite interface, there is no problem  * with embedding regular types inside lite types.  *  * @author kenton@google.com Kenton Varda  */
end_comment

begin_class
specifier|public
class|class
name|ExtensionRegistryLite
block|{
comment|// Set true to enable lazy parsing feature for MessageSet.
comment|//
comment|// TODO(xiangl): Now we use a global flag to control whether enable lazy
comment|// parsing feature for MessageSet, which may be too crude for some
comment|// applications. Need to support this feature on smaller granularity.
specifier|private
specifier|static
specifier|volatile
name|boolean
name|eagerlyParseMessageSets
init|=
literal|false
decl_stmt|;
comment|// Visible for testing.
specifier|static
specifier|final
name|String
name|EXTENSION_CLASS_NAME
init|=
literal|"org.apache.hadoop.hbase.shaded.com.google.protobuf.Extension"
decl_stmt|;
comment|/* @Nullable */
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|resolveExtensionClass
parameter_list|()
block|{
try|try
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
name|EXTENSION_CLASS_NAME
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// See comment in ExtensionRegistryFactory on the potential expense of this.
return|return
literal|null
return|;
block|}
block|}
comment|/* @Nullable */
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|extensionClass
init|=
name|resolveExtensionClass
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|isEagerlyParseMessageSets
parameter_list|()
block|{
return|return
name|eagerlyParseMessageSets
return|;
block|}
specifier|public
specifier|static
name|void
name|setEagerlyParseMessageSets
parameter_list|(
name|boolean
name|isEagerlyParse
parameter_list|)
block|{
name|eagerlyParseMessageSets
operator|=
name|isEagerlyParse
expr_stmt|;
block|}
comment|/**    * Construct a new, empty instance.    *    *<p>This may be an {@code ExtensionRegistry} if the full (non-Lite) proto libraries are    * available.    */
specifier|public
specifier|static
name|ExtensionRegistryLite
name|newInstance
parameter_list|()
block|{
return|return
name|ExtensionRegistryFactory
operator|.
name|create
argument_list|()
return|;
block|}
comment|/**    * Get the unmodifiable singleton empty instance of either ExtensionRegistryLite or    * {@code ExtensionRegistry} (if the full (non-Lite) proto libraries are available).    */
specifier|public
specifier|static
name|ExtensionRegistryLite
name|getEmptyRegistry
parameter_list|()
block|{
return|return
name|ExtensionRegistryFactory
operator|.
name|createEmpty
argument_list|()
return|;
block|}
comment|/** Returns an unmodifiable view of the registry. */
specifier|public
name|ExtensionRegistryLite
name|getUnmodifiable
parameter_list|()
block|{
return|return
operator|new
name|ExtensionRegistryLite
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Find an extension by containing type and field number.    *    * @return Information about the extension if found, or {@code null}    *         otherwise.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
parameter_list|<
name|ContainingType
extends|extends
name|MessageLite
parameter_list|>
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
argument_list|<
name|ContainingType
argument_list|,
name|?
argument_list|>
name|findLiteExtensionByNumber
parameter_list|(
specifier|final
name|ContainingType
name|containingTypeDefaultInstance
parameter_list|,
specifier|final
name|int
name|fieldNumber
parameter_list|)
block|{
return|return
operator|(
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
argument_list|<
name|ContainingType
argument_list|,
name|?
argument_list|>
operator|)
name|extensionsByNumber
operator|.
name|get
argument_list|(
operator|new
name|ObjectIntPair
argument_list|(
name|containingTypeDefaultInstance
argument_list|,
name|fieldNumber
argument_list|)
argument_list|)
return|;
block|}
comment|/** Add an extension from a lite generated file to the registry. */
specifier|public
specifier|final
name|void
name|add
parameter_list|(
specifier|final
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|extension
parameter_list|)
block|{
name|extensionsByNumber
operator|.
name|put
argument_list|(
operator|new
name|ObjectIntPair
argument_list|(
name|extension
operator|.
name|getContainingTypeDefaultInstance
argument_list|()
argument_list|,
name|extension
operator|.
name|getNumber
argument_list|()
argument_list|)
argument_list|,
name|extension
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add an extension from a lite generated file to the registry only if it is    * a non-lite extension i.e. {@link GeneratedMessageLite.GeneratedExtension}. */
specifier|public
specifier|final
name|void
name|add
parameter_list|(
name|ExtensionLite
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|extension
parameter_list|)
block|{
if|if
condition|(
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|extension
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
name|add
argument_list|(
operator|(
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|extension
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ExtensionRegistryFactory
operator|.
name|isFullRegistry
argument_list|(
name|this
argument_list|)
condition|)
block|{
try|try
block|{
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"add"
argument_list|,
name|extensionClass
argument_list|)
operator|.
name|invoke
argument_list|(
name|this
argument_list|,
name|extension
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Could not invoke ExtensionRegistry#add for %s"
argument_list|,
name|extension
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// =================================================================
comment|// Private stuff.
comment|// Constructors are package-private so that ExtensionRegistry can subclass
comment|// this.
name|ExtensionRegistryLite
parameter_list|()
block|{
name|this
operator|.
name|extensionsByNumber
operator|=
operator|new
name|HashMap
argument_list|<
name|ObjectIntPair
argument_list|,
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|static
specifier|final
name|ExtensionRegistryLite
name|EMPTY_REGISTRY_LITE
init|=
operator|new
name|ExtensionRegistryLite
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|ExtensionRegistryLite
parameter_list|(
name|ExtensionRegistryLite
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|EMPTY_REGISTRY_LITE
condition|)
block|{
name|this
operator|.
name|extensionsByNumber
operator|=
name|Collections
operator|.
name|emptyMap
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|extensionsByNumber
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|other
operator|.
name|extensionsByNumber
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|Map
argument_list|<
name|ObjectIntPair
argument_list|,
name|GeneratedMessageLite
operator|.
name|GeneratedExtension
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
name|extensionsByNumber
decl_stmt|;
name|ExtensionRegistryLite
parameter_list|(
name|boolean
name|empty
parameter_list|)
block|{
name|this
operator|.
name|extensionsByNumber
operator|=
name|Collections
operator|.
name|emptyMap
argument_list|()
expr_stmt|;
block|}
comment|/** A (Object, int) pair, used as a map key. */
specifier|private
specifier|static
specifier|final
class|class
name|ObjectIntPair
block|{
specifier|private
specifier|final
name|Object
name|object
decl_stmt|;
specifier|private
specifier|final
name|int
name|number
decl_stmt|;
name|ObjectIntPair
parameter_list|(
specifier|final
name|Object
name|object
parameter_list|,
specifier|final
name|int
name|number
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
name|this
operator|.
name|number
operator|=
name|number
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|System
operator|.
name|identityHashCode
argument_list|(
name|object
argument_list|)
operator|*
operator|(
operator|(
literal|1
operator|<<
literal|16
operator|)
operator|-
literal|1
operator|)
operator|+
name|number
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|ObjectIntPair
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|ObjectIntPair
name|other
init|=
operator|(
name|ObjectIntPair
operator|)
name|obj
decl_stmt|;
return|return
name|object
operator|==
name|other
operator|.
name|object
operator|&&
name|number
operator|==
name|other
operator|.
name|number
return|;
block|}
block|}
block|}
end_class

end_unit


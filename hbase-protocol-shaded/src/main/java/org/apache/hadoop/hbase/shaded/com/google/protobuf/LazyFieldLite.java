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
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * LazyFieldLite encapsulates the logic of lazily parsing message fields. It stores  * the message in a ByteString initially and then parses it on-demand.  *  * LazyFieldLite is thread-compatible: concurrent reads are safe once the proto that this  * LazyFieldLite is a part of is no longer being mutated by its Builder. However, explicit  * synchronization is needed under read/write situations.  *  * When a LazyFieldLite is used in the context of a MessageLite object, its behavior is considered  * to be immutable and none of the setter methods in its API are expected to be invoked. All of the  * getters are expected to be thread-safe. When used in the context of a MessageLite.Builder,  * setters can be invoked, but there is no guarantee of thread safety.  *   * TODO(yatin,dweis): Consider splitting this class's functionality and put the mutable methods  * into a separate builder class to allow us to give stronger compile-time guarantees.  *  * This class is internal implementation detail of the protobuf library, so you don't need to use it  * directly.  *  * @author xiangl@google.com (Xiang Li)  */
end_comment

begin_class
specifier|public
class|class
name|LazyFieldLite
block|{
specifier|private
specifier|static
specifier|final
name|ExtensionRegistryLite
name|EMPTY_REGISTRY
init|=
name|ExtensionRegistryLite
operator|.
name|getEmptyRegistry
argument_list|()
decl_stmt|;
comment|/**    * The value associated with the LazyFieldLite object is stored in one or more of the following    * three fields (delayedBytes, value, memoizedBytes). They should together be interpreted as    * follows.    * 1) delayedBytes can be non-null, while value and memoizedBytes is null. The object will be in    *    this state while the value for the object has not yet been parsed.    * 2) Both delayedBytes and value are non-null. The object transitions to this state as soon as    *    some caller needs to access the value (by invoking getValue()).    * 3) memoizedBytes is merely an optimization for calls to LazyFieldLite.toByteString() to avoid    *    recomputing the ByteString representation on each call. Instead, when the value is parsed    *    from delayedBytes, we will also assign the contents of delayedBytes to memoizedBytes (since    *    that is the ByteString representation of value).    * 4) Finally, if the LazyFieldLite was created directly with a parsed MessageLite value, then    *    delayedBytes will be null, and memoizedBytes will be initialized only upon the first call to    *    LazyFieldLite.toByteString().    *    * Given the above conditions, any caller that needs a serialized representation of this object    * must first check if the memoizedBytes or delayedBytes ByteString is non-null and use it    * directly; if both of those are null, it can look at the parsed value field. Similarly, any    * caller that needs a parsed value must first check if the value field is already non-null, if    * not it must parse the value from delayedBytes.    */
comment|/**    * A delayed-parsed version of the contents of this field. When this field is non-null, then the    * "value" field is allowed to be null until the time that the value needs to be read.    *    * When delayedBytes is non-null then {@code extensionRegistry} is required to also be non-null.    * {@code value} and {@code memoizedBytes} will be initialized lazily.    */
specifier|private
name|ByteString
name|delayedBytes
decl_stmt|;
comment|/**    * An {@code ExtensionRegistryLite} for parsing bytes. It is non-null on a best-effort basis. It    * is only guaranteed to be non-null if this message was initialized using bytes and an    * {@code ExtensionRegistry}. If it directly had a value set then it will be null, unless it has    * been merged with another {@code LazyFieldLite} that had an {@code ExtensionRegistry}.    */
specifier|private
name|ExtensionRegistryLite
name|extensionRegistry
decl_stmt|;
comment|/**    * The parsed value. When this is null and a caller needs access to the MessageLite value, then    * {@code delayedBytes} will be parsed lazily at that time.    */
specifier|protected
specifier|volatile
name|MessageLite
name|value
decl_stmt|;
comment|/**    * The memoized bytes for {@code value}. This is an optimization for the toByteString() method to    * not have to recompute its return-value on each invocation.    * TODO(yatin): Figure out whether this optimization is actually necessary.    */
specifier|private
specifier|volatile
name|ByteString
name|memoizedBytes
decl_stmt|;
comment|/**    * Constructs a LazyFieldLite with bytes that will be parsed lazily.    */
specifier|public
name|LazyFieldLite
parameter_list|(
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|,
name|ByteString
name|bytes
parameter_list|)
block|{
name|checkArguments
argument_list|(
name|extensionRegistry
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|this
operator|.
name|extensionRegistry
operator|=
name|extensionRegistry
expr_stmt|;
name|this
operator|.
name|delayedBytes
operator|=
name|bytes
expr_stmt|;
block|}
comment|/**    * Constructs a LazyFieldLite with no contents, and no ability to parse extensions.    */
specifier|public
name|LazyFieldLite
parameter_list|()
block|{   }
comment|/**    * Constructs a LazyFieldLite instance with a value. The LazyFieldLite may not be able to parse    * the extensions in the value as it has no ExtensionRegistry.    */
specifier|public
specifier|static
name|LazyFieldLite
name|fromValue
parameter_list|(
name|MessageLite
name|value
parameter_list|)
block|{
name|LazyFieldLite
name|lf
init|=
operator|new
name|LazyFieldLite
argument_list|()
decl_stmt|;
name|lf
operator|.
name|setValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|lf
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|LazyFieldLite
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|LazyFieldLite
name|other
init|=
operator|(
name|LazyFieldLite
operator|)
name|o
decl_stmt|;
comment|// Lazy fields do not work well with equals... If both are delayedBytes, we do not have a
comment|// mechanism to deserialize them so we rely on bytes equality. Otherwise we coerce into an
comment|// actual message (if necessary) and call equals on the message itself. This implies that two
comment|// messages can by unequal but then be turned equal simply be invoking a getter on a lazy field.
name|MessageLite
name|value1
init|=
name|value
decl_stmt|;
name|MessageLite
name|value2
init|=
name|other
operator|.
name|value
decl_stmt|;
if|if
condition|(
name|value1
operator|==
literal|null
operator|&&
name|value2
operator|==
literal|null
condition|)
block|{
return|return
name|toByteString
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|toByteString
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|value1
operator|!=
literal|null
operator|&&
name|value2
operator|!=
literal|null
condition|)
block|{
return|return
name|value1
operator|.
name|equals
argument_list|(
name|value2
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|value1
operator|!=
literal|null
condition|)
block|{
return|return
name|value1
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getValue
argument_list|(
name|value1
operator|.
name|getDefaultInstanceForType
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|getValue
argument_list|(
name|value2
operator|.
name|getDefaultInstanceForType
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|value2
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
comment|// We can't provide a memoizable hash code for lazy fields. The byte strings may have different
comment|// hash codes but evaluate to equivalent messages. And we have no facility for constructing
comment|// a message here if we were not already holding a value.
return|return
literal|1
return|;
block|}
comment|/**    * Determines whether this LazyFieldLite instance represents the default instance of this type.    */
specifier|public
name|boolean
name|containsDefaultInstance
parameter_list|()
block|{
return|return
name|memoizedBytes
operator|==
name|ByteString
operator|.
name|EMPTY
operator|||
name|value
operator|==
literal|null
operator|&&
operator|(
name|delayedBytes
operator|==
literal|null
operator|||
name|delayedBytes
operator|==
name|ByteString
operator|.
name|EMPTY
operator|)
return|;
block|}
comment|/**    * Clears the value state of this instance.    *    *<p>LazyField is not thread-safe for write access. Synchronizations are needed    * under read/write situations.    */
specifier|public
name|void
name|clear
parameter_list|()
block|{
comment|// Don't clear the ExtensionRegistry. It might prove useful later on when merging in another
comment|// value, but there is no guarantee that it will contain all extensions that were directly set
comment|// on the values that need to be merged.
name|delayedBytes
operator|=
literal|null
expr_stmt|;
name|value
operator|=
literal|null
expr_stmt|;
name|memoizedBytes
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Overrides the contents of this LazyField.    *    *<p>LazyField is not thread-safe for write access. Synchronizations are needed    * under read/write situations.    */
specifier|public
name|void
name|set
parameter_list|(
name|LazyFieldLite
name|other
parameter_list|)
block|{
name|this
operator|.
name|delayedBytes
operator|=
name|other
operator|.
name|delayedBytes
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|other
operator|.
name|value
expr_stmt|;
name|this
operator|.
name|memoizedBytes
operator|=
name|other
operator|.
name|memoizedBytes
expr_stmt|;
comment|// If the other LazyFieldLite was created by directly setting the value rather than first by
comment|// parsing, then it will not have an extensionRegistry. In this case we hold on to the existing
comment|// extensionRegistry, which has no guarantees that it has all the extensions that will be
comment|// directly set on the value.
if|if
condition|(
name|other
operator|.
name|extensionRegistry
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|extensionRegistry
operator|=
name|other
operator|.
name|extensionRegistry
expr_stmt|;
block|}
block|}
comment|/**    * Returns message instance. It may do some thread-safe delayed parsing of bytes.    *    * @param defaultInstance its message's default instance. It's also used to get parser for the    * message type.    */
specifier|public
name|MessageLite
name|getValue
parameter_list|(
name|MessageLite
name|defaultInstance
parameter_list|)
block|{
name|ensureInitialized
argument_list|(
name|defaultInstance
argument_list|)
expr_stmt|;
return|return
name|value
return|;
block|}
comment|/**    * Sets the value of the instance and returns the old value without delay parsing anything.    *    *<p>LazyField is not thread-safe for write access. Synchronizations are needed    * under read/write situations.    */
specifier|public
name|MessageLite
name|setValue
parameter_list|(
name|MessageLite
name|value
parameter_list|)
block|{
name|MessageLite
name|originalValue
init|=
name|this
operator|.
name|value
decl_stmt|;
name|this
operator|.
name|delayedBytes
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|memoizedBytes
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
return|return
name|originalValue
return|;
block|}
comment|/**    * Merges another instance's contents. In some cases may drop some extensions if both fields    * contain data. If the other field has an {@code ExtensionRegistry} but this does not, then this    * field will copy over that {@code ExtensionRegistry}.    *    *<p>LazyField is not thread-safe for write access. Synchronizations are needed    * under read/write situations.    */
specifier|public
name|void
name|merge
parameter_list|(
name|LazyFieldLite
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|containsDefaultInstance
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|this
operator|.
name|containsDefaultInstance
argument_list|()
condition|)
block|{
name|set
argument_list|(
name|other
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// If the other field has an extension registry but this does not, copy over the other extension
comment|// registry.
if|if
condition|(
name|this
operator|.
name|extensionRegistry
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|extensionRegistry
operator|=
name|other
operator|.
name|extensionRegistry
expr_stmt|;
block|}
comment|// In the case that both of them are not parsed we simply concatenate the bytes to save time. In
comment|// the (probably rare) case that they have different extension registries there is a chance that
comment|// some of the extensions may be dropped, but the tradeoff of making this operation fast seems
comment|// to outway the benefits of combining the extension registries, which is not normally done for
comment|// lite protos anyways.
if|if
condition|(
name|this
operator|.
name|delayedBytes
operator|!=
literal|null
operator|&&
name|other
operator|.
name|delayedBytes
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|delayedBytes
operator|=
name|this
operator|.
name|delayedBytes
operator|.
name|concat
argument_list|(
name|other
operator|.
name|delayedBytes
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// At least one is parsed and both contain data. We won't drop any extensions here directly, but
comment|// in the case that the extension registries are not the same then we might in the future if we
comment|// need to serialze and parse a message again.
if|if
condition|(
name|this
operator|.
name|value
operator|==
literal|null
operator|&&
name|other
operator|.
name|value
operator|!=
literal|null
condition|)
block|{
name|setValue
argument_list|(
name|mergeValueAndBytes
argument_list|(
name|other
operator|.
name|value
argument_list|,
name|this
operator|.
name|delayedBytes
argument_list|,
name|this
operator|.
name|extensionRegistry
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|value
operator|!=
literal|null
operator|&&
name|other
operator|.
name|value
operator|==
literal|null
condition|)
block|{
name|setValue
argument_list|(
name|mergeValueAndBytes
argument_list|(
name|this
operator|.
name|value
argument_list|,
name|other
operator|.
name|delayedBytes
argument_list|,
name|other
operator|.
name|extensionRegistry
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// At this point we have two fully parsed messages. We can't merge directly from one to the
comment|// other because only generated builder code contains methods to mergeFrom another parsed
comment|// message. We have to serialize one instance and then merge the bytes into the other. This may
comment|// drop extensions from one of the messages if one of the values had an extension set on it
comment|// directly.
comment|//
comment|// To mitigate this we prefer serializing a message that has an extension registry, and
comment|// therefore a chance that all extensions set on it are in that registry.
comment|//
comment|// NOTE: The check for other.extensionRegistry not being null must come first because at this
comment|// point in time if other.extensionRegistry is not null then this.extensionRegistry will not be
comment|// null either.
if|if
condition|(
name|other
operator|.
name|extensionRegistry
operator|!=
literal|null
condition|)
block|{
name|setValue
argument_list|(
name|mergeValueAndBytes
argument_list|(
name|this
operator|.
name|value
argument_list|,
name|other
operator|.
name|toByteString
argument_list|()
argument_list|,
name|other
operator|.
name|extensionRegistry
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|extensionRegistry
operator|!=
literal|null
condition|)
block|{
name|setValue
argument_list|(
name|mergeValueAndBytes
argument_list|(
name|other
operator|.
name|value
argument_list|,
name|this
operator|.
name|toByteString
argument_list|()
argument_list|,
name|this
operator|.
name|extensionRegistry
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
comment|// All extensions from the other message will be dropped because we have no registry.
name|setValue
argument_list|(
name|mergeValueAndBytes
argument_list|(
name|this
operator|.
name|value
argument_list|,
name|other
operator|.
name|toByteString
argument_list|()
argument_list|,
name|EMPTY_REGISTRY
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|/**    * Merges another instance's contents from a stream.    *    *<p>LazyField is not thread-safe for write access. Synchronizations are needed    * under read/write situations.    */
specifier|public
name|void
name|mergeFrom
parameter_list|(
name|CodedInputStream
name|input
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|containsDefaultInstance
argument_list|()
condition|)
block|{
name|setByteString
argument_list|(
name|input
operator|.
name|readBytes
argument_list|()
argument_list|,
name|extensionRegistry
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// If the other field has an extension registry but this does not, copy over the other extension
comment|// registry.
if|if
condition|(
name|this
operator|.
name|extensionRegistry
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|extensionRegistry
operator|=
name|extensionRegistry
expr_stmt|;
block|}
comment|// In the case that both of them are not parsed we simply concatenate the bytes to save time. In
comment|// the (probably rare) case that they have different extension registries there is a chance that
comment|// some of the extensions may be dropped, but the tradeoff of making this operation fast seems
comment|// to outway the benefits of combining the extension registries, which is not normally done for
comment|// lite protos anyways.
if|if
condition|(
name|this
operator|.
name|delayedBytes
operator|!=
literal|null
condition|)
block|{
name|setByteString
argument_list|(
name|this
operator|.
name|delayedBytes
operator|.
name|concat
argument_list|(
name|input
operator|.
name|readBytes
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|extensionRegistry
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// We are parsed and both contain data. We won't drop any extensions here directly, but in the
comment|// case that the extension registries are not the same then we might in the future if we
comment|// need to serialize and parse a message again.
try|try
block|{
name|setValue
argument_list|(
name|value
operator|.
name|toBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|input
argument_list|,
name|extensionRegistry
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
comment|// Nothing is logged and no exceptions are thrown. Clients will be unaware that a proto
comment|// was invalid.
block|}
block|}
specifier|private
specifier|static
name|MessageLite
name|mergeValueAndBytes
parameter_list|(
name|MessageLite
name|value
parameter_list|,
name|ByteString
name|otherBytes
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
block|{
try|try
block|{
return|return
name|value
operator|.
name|toBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|otherBytes
argument_list|,
name|extensionRegistry
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
comment|// Nothing is logged and no exceptions are thrown. Clients will be unaware that a proto
comment|// was invalid.
return|return
name|value
return|;
block|}
block|}
comment|/**    * Sets this field with bytes to delay-parse.    */
specifier|public
name|void
name|setByteString
parameter_list|(
name|ByteString
name|bytes
parameter_list|,
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
block|{
name|checkArguments
argument_list|(
name|extensionRegistry
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|this
operator|.
name|delayedBytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|extensionRegistry
operator|=
name|extensionRegistry
expr_stmt|;
name|this
operator|.
name|value
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|memoizedBytes
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Due to the optional field can be duplicated at the end of serialized    * bytes, which will make the serialized size changed after LazyField    * parsed. Be careful when using this method.    */
specifier|public
name|int
name|getSerializedSize
parameter_list|()
block|{
comment|// We *must* return delayed bytes size if it was ever set because the dependent messages may
comment|// have memoized serialized size based off of it.
if|if
condition|(
name|memoizedBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|memoizedBytes
operator|.
name|size
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|delayedBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|delayedBytes
operator|.
name|size
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
operator|.
name|getSerializedSize
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
comment|/**    * Returns a BytesString for this field in a thread-safe way.    */
specifier|public
name|ByteString
name|toByteString
parameter_list|()
block|{
if|if
condition|(
name|memoizedBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|memoizedBytes
return|;
block|}
comment|// We *must* return delayed bytes if it was set because the dependent messages may have
comment|// memoized serialized size based off of it.
if|if
condition|(
name|delayedBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|delayedBytes
return|;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|memoizedBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|memoizedBytes
return|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|memoizedBytes
operator|=
name|ByteString
operator|.
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|memoizedBytes
operator|=
name|value
operator|.
name|toByteString
argument_list|()
expr_stmt|;
block|}
return|return
name|memoizedBytes
return|;
block|}
block|}
comment|/**    * Might lazily parse the bytes that were previously passed in. Is thread-safe.    */
specifier|protected
name|void
name|ensureInitialized
parameter_list|(
name|MessageLite
name|defaultInstance
parameter_list|)
block|{
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
try|try
block|{
if|if
condition|(
name|delayedBytes
operator|!=
literal|null
condition|)
block|{
comment|// The extensionRegistry shouldn't be null here since we have delayedBytes.
name|MessageLite
name|parsedValue
init|=
name|defaultInstance
operator|.
name|getParserForType
argument_list|()
operator|.
name|parseFrom
argument_list|(
name|delayedBytes
argument_list|,
name|extensionRegistry
argument_list|)
decl_stmt|;
name|this
operator|.
name|value
operator|=
name|parsedValue
expr_stmt|;
name|this
operator|.
name|memoizedBytes
operator|=
name|delayedBytes
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|value
operator|=
name|defaultInstance
expr_stmt|;
name|this
operator|.
name|memoizedBytes
operator|=
name|ByteString
operator|.
name|EMPTY
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
comment|// Nothing is logged and no exceptions are thrown. Clients will be unaware that this proto
comment|// was invalid.
name|this
operator|.
name|value
operator|=
name|defaultInstance
expr_stmt|;
name|this
operator|.
name|memoizedBytes
operator|=
name|ByteString
operator|.
name|EMPTY
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|checkArguments
parameter_list|(
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|,
name|ByteString
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|extensionRegistry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"found null ExtensionRegistry"
argument_list|)
throw|;
block|}
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"found null ByteString"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


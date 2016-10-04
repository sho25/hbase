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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * An interface extending {@code List<String>} that also provides access to the  * items of the list as UTF8-encoded ByteString or byte[] objects. This is  * used by the protocol buffer implementation to support lazily converting bytes  * parsed over the wire to String objects until needed and also increases the  * efficiency of serialization if the String was never requested as the  * ByteString or byte[] is already cached. The ByteString methods are used in  * immutable API only and byte[] methods used in mutable API only for they use  * different representations for string/bytes fields.  *  * @author jonp@google.com (Jon Perlow)  */
end_comment

begin_interface
specifier|public
interface|interface
name|LazyStringList
extends|extends
name|ProtocolStringList
block|{
comment|/**    * Returns the element at the specified position in this list as a ByteString.    *    * @param index index of the element to return    * @return the element at the specified position in this list    * @throws IndexOutOfBoundsException if the index is out of range    *         ({@code index< 0 || index>= size()})    */
name|ByteString
name|getByteString
parameter_list|(
name|int
name|index
parameter_list|)
function_decl|;
comment|/**    * Returns the element at the specified position in this list as an Object    * that will either be a String or a ByteString.    *    * @param index index of the element to return    * @return the element at the specified position in this list    * @throws IndexOutOfBoundsException if the index is out of range    *         ({@code index< 0 || index>= size()})    */
name|Object
name|getRaw
parameter_list|(
name|int
name|index
parameter_list|)
function_decl|;
comment|/**    * Returns the element at the specified position in this list as byte[].    *    * @param index index of the element to return    * @return the element at the specified position in this list    * @throws IndexOutOfBoundsException if the index is out of range    *         ({@code index< 0 || index>= size()})    */
name|byte
index|[]
name|getByteArray
parameter_list|(
name|int
name|index
parameter_list|)
function_decl|;
comment|/**    * Appends the specified element to the end of this list (optional    * operation).    *    * @param element element to be appended to this list    * @throws UnsupportedOperationException if the<tt>add</tt> operation    *         is not supported by this list    */
name|void
name|add
parameter_list|(
name|ByteString
name|element
parameter_list|)
function_decl|;
comment|/**    * Appends the specified element to the end of this list (optional    * operation).    *    * @param element element to be appended to this list    * @throws UnsupportedOperationException if the<tt>add</tt> operation    *         is not supported by this list    */
name|void
name|add
parameter_list|(
name|byte
index|[]
name|element
parameter_list|)
function_decl|;
comment|/**    * Replaces the element at the specified position in this list with the    * specified element (optional operation).    *    * @param index index of the element to replace    * @param element the element to be stored at the specified position    * @throws UnsupportedOperationException if the<tt>set</tt> operation    *         is not supported by this list    *         IndexOutOfBoundsException if the index is out of range    *         ({@code index< 0 || index>= size()})    */
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|ByteString
name|element
parameter_list|)
function_decl|;
comment|/**    * Replaces the element at the specified position in this list with the    * specified element (optional operation).    *    * @param index index of the element to replace    * @param element the element to be stored at the specified position    * @throws UnsupportedOperationException if the<tt>set</tt> operation    *         is not supported by this list    *         IndexOutOfBoundsException if the index is out of range    *         ({@code index< 0 || index>= size()})    */
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|element
parameter_list|)
function_decl|;
comment|/**    * Appends all elements in the specified ByteString collection to the end of    * this list.    *    * @param c collection whose elements are to be added to this list    * @return true if this list changed as a result of the call    * @throws UnsupportedOperationException if the<tt>addAllByteString</tt>    *         operation is not supported by this list    */
name|boolean
name|addAllByteString
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|ByteString
argument_list|>
name|c
parameter_list|)
function_decl|;
comment|/**    * Appends all elements in the specified byte[] collection to the end of    * this list.    *    * @param c collection whose elements are to be added to this list    * @return true if this list changed as a result of the call    * @throws UnsupportedOperationException if the<tt>addAllByteArray</tt>    *         operation is not supported by this list    */
name|boolean
name|addAllByteArray
parameter_list|(
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|c
parameter_list|)
function_decl|;
comment|/**    * Returns an unmodifiable List of the underlying elements, each of which is    * either a {@code String} or its equivalent UTF-8 encoded {@code ByteString}    * or byte[]. It is an error for the caller to modify the returned    * List, and attempting to do so will result in an    * {@link UnsupportedOperationException}.    */
name|List
argument_list|<
name|?
argument_list|>
name|getUnderlyingElements
parameter_list|()
function_decl|;
comment|/**    * Merges all elements from another LazyStringList into this one. This method    * differs from {@link #addAll(Collection)} on that underlying byte arrays are    * copied instead of reference shared. Immutable API doesn't need to use this    * method as byte[] is not used there at all.    */
name|void
name|mergeFrom
parameter_list|(
name|LazyStringList
name|other
parameter_list|)
function_decl|;
comment|/**    * Returns a mutable view of this list. Changes to the view will be made into    * the original list. This method is used in mutable API only.    */
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|asByteArrayList
parameter_list|()
function_decl|;
comment|/** Returns an unmodifiable view of the list. */
name|LazyStringList
name|getUnmodifiableView
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


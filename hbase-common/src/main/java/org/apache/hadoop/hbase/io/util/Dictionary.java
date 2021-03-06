begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|util
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ByteBufferUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Dictionary interface  *  * Dictionary indexes should be either bytes or shorts, only positive. (The  * first bit is reserved for detecting whether something is compressed or not).  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Dictionary
block|{
name|byte
name|NOT_IN_DICTIONARY
init|=
operator|-
literal|1
decl_stmt|;
name|void
name|init
parameter_list|(
name|int
name|initialSize
parameter_list|)
function_decl|;
comment|/**    * Gets an entry from the dictionary.    *     * @param idx index of the entry    * @return the entry, or null if non existent    */
name|byte
index|[]
name|getEntry
parameter_list|(
name|short
name|idx
parameter_list|)
function_decl|;
comment|/**    * Finds the index of an entry.    * If no entry found, we add it.    *     * @param data the byte array that we're looking up    * @param offset Offset into<code>data</code> to add to Dictionary.    * @param length Length beyond<code>offset</code> that comprises entry; must be&gt; 0.    * @return the index of the entry, or {@link #NOT_IN_DICTIONARY} if not found    */
name|short
name|findEntry
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * Finds the index of an entry.    * If no entry found, we add it.    * @param data the ByteBuffer that we're looking up    * @param offset Offset into<code>data</code> to add to Dictionary.    * @param length Length beyond<code>offset</code> that comprises entry; must be&gt; 0.    * @return the index of the entry, or {@link #NOT_IN_DICTIONARY} if not found    */
name|short
name|findEntry
parameter_list|(
name|ByteBuffer
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * Adds an entry to the dictionary.    * Be careful using this method.  It will add an entry to the    * dictionary even if it already has an entry for the same data.    * Call {{@link #findEntry(byte[], int, int)}} to add without duplicating    * dictionary entries.    *     * @param data the entry to add    * @param offset Offset into<code>data</code> to add to Dictionary.    * @param length Length beyond<code>offset</code> that comprises entry; must be&gt; 0.    * @return the index of the entry    */
name|short
name|addEntry
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * Flushes the dictionary, empties all values.    */
name|void
name|clear
parameter_list|()
function_decl|;
comment|/**    * Helper methods to write the dictionary data to the OutputStream    * @param out the outputstream to which data needs to be written    * @param data the data to be written in byte[]    * @param offset the offset    * @param length length to be written    * @param dict the dictionary whose contents are to written    * @throws IOException    */
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|short
name|dictIdx
init|=
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
decl_stmt|;
if|if
condition|(
name|dict
operator|!=
literal|null
condition|)
block|{
name|dictIdx
operator|=
name|dict
operator|.
name|findEntry
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dictIdx
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|StreamUtils
operator|.
name|writeShort
argument_list|(
name|out
argument_list|,
name|dictIdx
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Helper methods to write the dictionary data to the OutputStream    * @param out the outputstream to which data needs to be written    * @param data the data to be written in ByteBuffer    * @param offset the offset    * @param length length to be written    * @param dict the dictionary whose contents are to written    * @throws IOException    */
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|ByteBuffer
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|short
name|dictIdx
init|=
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
decl_stmt|;
if|if
condition|(
name|dict
operator|!=
literal|null
condition|)
block|{
name|dictIdx
operator|=
name|dict
operator|.
name|findEntry
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dictIdx
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
argument_list|)
expr_stmt|;
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|out
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyBufferToStream
argument_list|(
name|out
argument_list|,
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|StreamUtils
operator|.
name|writeShort
argument_list|(
name|out
argument_list|,
name|dictIdx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_interface

end_unit


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
name|regionserver
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
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
interface|interface
name|Dictionary
block|{
name|byte
name|NOT_IN_DICTIONARY
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * Gets an entry from the dictionary.    *     * @param idx index of the entry    * @return the entry, or null if non existent    */
name|byte
index|[]
name|getEntry
parameter_list|(
name|short
name|idx
parameter_list|)
function_decl|;
comment|/**    * Finds the index of an entry.    * If no entry found, we add it.    *     * @param data the byte array that we're looking up    * @param offset Offset into<code>data</code> to add to Dictionary.    * @param length Length beyond<code>offset</code> that comprises entry; must be> 0.    * @return the index of the entry, or {@link #NOT_IN_DICTIONARY} if not found    */
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
comment|/**    * Adds an entry to the dictionary.    * Be careful using this method.  It will add an entry to the    * dictionary even if it already has an entry for the same data.    * Call {{@link #findEntry(byte[], int, int)}} to add without duplicating    * dictionary entries.    *     * @param data the entry to add    * @param offset Offset into<code>data</code> to add to Dictionary.    * @param length Length beyond<code>offset</code> that comprises entry; must be> 0.    * @return the index of the entry    */
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
block|}
end_interface

end_unit


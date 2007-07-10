begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|TreeMap
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Internally, we need to be able to determine if the scanner is doing wildcard  * column matches (when only a column family is specified or if a column regex  * is specified) or if multiple members of the same column family were  * specified. If so, we need to ignore the timestamp to ensure that we get all  * the family members, as they may have been last updated at different times.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HInternalScannerInterface
block|{
comment|/**    * Grab the next row's worth of values. The HScanner will return the most    * recent data value for each row that is not newer than the target time.    *     * If a dataFilter is defined, it will be used to skip rows that do not    * match its criteria. It may cause the scanner to stop prematurely if it    * knows that it will no longer accept the remaining results.    *     * @param key HStoreKey containing row and timestamp    * @param results Map of column/value pairs    * @return true if a value was found    * @throws IOException    */
specifier|public
name|boolean
name|next
parameter_list|(
name|HStoreKey
name|key
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Close the scanner.    */
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
comment|/** @return true if the scanner is matching a column family or regex */
specifier|public
name|boolean
name|isWildcardScanner
parameter_list|()
function_decl|;
comment|/** @return true if the scanner is matching multiple column family members */
specifier|public
name|boolean
name|isMultipleMatchScanner
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


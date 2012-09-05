begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HRegionInfo
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
name|client
operator|.
name|Scan
import|;
end_import

begin_comment
comment|/**  * RegionScanner describes iterators over rows in an HRegion.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RegionScanner
extends|extends
name|InternalScanner
block|{
comment|/**    * @return The RegionInfo for this scanner.    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
function_decl|;
comment|/**    * @return True if a filter indicates that this scanner will return no    *         further rows.    */
specifier|public
name|boolean
name|isFilterDone
parameter_list|()
function_decl|;
comment|/**    * Do a reseek to the required row. Should not be used to seek to a key which    * may come before the current position. Always seeks to the beginning of a    * row boundary.    *    * @throws IOException    * @throws IllegalArgumentException    *           if row is null    *    */
specifier|public
name|boolean
name|reseek
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return The preferred max buffersize. See {@link Scan#setMaxResultSize(long)}    */
specifier|public
name|long
name|getMaxResultSize
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


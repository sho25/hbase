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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

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
name|List
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
name|HRegionLocation
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
name|TableName
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * Used to view region location information for a single HBase table.  * Obtain an instance from an {@link Connection}.  *  * @see ConnectionFactory  * @see Connection  * @see Table  * @since 0.99.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|RegionLocator
extends|extends
name|Closeable
block|{
comment|/**    * Finds the region on which the given row is being served. Does not reload the cache.    * @param row Row to find.    * @return Location of the row.    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Finds the region on which the given row is being served.    * @param row Row to find.    * @param reload true to reload information or false to use cached information    * @return Location of the row.    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieves all of the regions associated with this table.    * @return a {@link List} of all regions associated with this table.    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getAllRegionLocations
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the starting row key for every region in the currently open table.    *<p>    * This is mainly useful for the MapReduce integration.    * @return Array of region starting row keys    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|byte
index|[]
index|[]
name|getStartKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the ending row key for every region in the currently open table.    *<p>    * This is mainly useful for the MapReduce integration.    * @return Array of region ending row keys    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|byte
index|[]
index|[]
name|getEndKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the starting and ending row keys for every region in the currently    * open table.    *<p>    * This is mainly useful for the MapReduce integration.    * @return Pair of arrays of region starting and ending row keys    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|getStartEndKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets the fully qualified table name instance of this table.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


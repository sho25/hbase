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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
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
name|hadoop
operator|.
name|hbase
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * The asynchronous version of RegionLocator.  *<p>  * Usually the implementations will not throw any exception directly, you need to get the exception  * from the returned {@link CompletableFuture}.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|AsyncTableRegionLocator
block|{
comment|/**    * Gets the fully qualified table name instance of the table whose region we want to locate.    */
name|TableName
name|getName
parameter_list|()
function_decl|;
comment|/**    * Finds the region on which the given row is being served. Does not reload the cache.    *<p>    * Returns the location of the region to which the row belongs.    * @param row Row to find.    */
specifier|default
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
name|getRegionLocation
argument_list|(
name|row
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Finds the region on which the given row is being served.    *<p>    * Returns the location of the region to which the row belongs.    * @param row Row to find.    * @param reload true to reload information or false to use cached information    */
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
function_decl|;
block|}
end_interface

end_unit


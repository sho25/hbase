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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * The class that contains shared information about various knobs of a Store/HStore object.  * Unlike the configuration objects that merely return the XML values, the implementations  * should return ready-to-use applicable values for corresponding calls, after all the  * parsing/validation/adjustment for other considerations, so that we don't have to repeat  * this logic in multiple places.  * TODO: move methods and logic here as necessary.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|StoreConfiguration
block|{
comment|/**    * Gets the cf-specific major compaction period.    */
specifier|public
name|Long
name|getMajorCompactionPeriod
parameter_list|()
function_decl|;
comment|/**    * Gets the Memstore flush size for the region that this store works with.    */
specifier|public
name|long
name|getMemstoreFlushSize
parameter_list|()
function_decl|;
comment|/**    * Gets the cf-specific time-to-live for store files.    */
specifier|public
name|long
name|getStoreFileTtl
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


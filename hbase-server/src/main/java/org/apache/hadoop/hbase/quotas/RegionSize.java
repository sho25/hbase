begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|hbase
operator|.
name|io
operator|.
name|HeapSize
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
comment|/**  * Interface that encapsulates optionally sending a Region's size to the master.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RegionSize
extends|extends
name|HeapSize
block|{
comment|/**    * Updates the size of the Region.    *    * @param newSize the new size of the Region    * @return {@code this}    */
name|RegionSize
name|setSize
parameter_list|(
name|long
name|newSize
parameter_list|)
function_decl|;
comment|/**    * Atomically adds the provided {@code delta} to the region size.    *    * @param delta The change in size in bytes of the region.    * @return {@code this}    */
name|RegionSize
name|incrementSize
parameter_list|(
name|long
name|delta
parameter_list|)
function_decl|;
comment|/**    * Returns the size of the region.    *    * @return The size in bytes.    */
name|long
name|getSize
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


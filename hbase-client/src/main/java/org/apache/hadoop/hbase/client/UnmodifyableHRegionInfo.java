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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
class|class
name|UnmodifyableHRegionInfo
extends|extends
name|HRegionInfo
block|{
comment|/*    * Creates an unmodifyable copy of an HRegionInfo    *    * @param info    */
name|UnmodifyableHRegionInfo
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
block|{
name|super
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param split set split status    */
annotation|@
name|Override
specifier|public
name|void
name|setSplit
parameter_list|(
name|boolean
name|split
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HRegionInfo is read-only"
argument_list|)
throw|;
block|}
comment|/**    * @param offLine set online - offline status    */
annotation|@
name|Override
specifier|public
name|void
name|setOffline
parameter_list|(
name|boolean
name|offLine
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"HRegionInfo is read-only"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit


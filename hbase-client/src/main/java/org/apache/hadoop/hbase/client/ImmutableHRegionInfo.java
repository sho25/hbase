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
name|HRegionInfo
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
comment|/**  * Read-only Region info.  */
end_comment

begin_class
annotation|@
name|Deprecated
comment|// deprecated for hbase 2.0, remove for hbase 3.0. see HRegionInfo.
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ImmutableHRegionInfo
extends|extends
name|HRegionInfo
block|{
comment|/*    * Creates an immutable copy of an HRegionInfo.    *    * @param other    */
specifier|public
name|ImmutableHRegionInfo
parameter_list|(
name|HRegionInfo
name|other
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
block|}
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
annotation|@
name|Override
specifier|public
name|void
name|setOffline
parameter_list|(
name|boolean
name|offline
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

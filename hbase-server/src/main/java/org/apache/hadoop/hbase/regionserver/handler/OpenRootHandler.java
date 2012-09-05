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
operator|.
name|handler
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
name|HTableDescriptor
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
name|Server
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
name|regionserver
operator|.
name|RegionServerServices
import|;
end_import

begin_comment
comment|/**  * Handles opening of the root region on a region server.  *<p>  * This is executed after receiving an OPEN RPC from the master for root.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|OpenRootHandler
extends|extends
name|OpenRegionHandler
block|{
specifier|public
name|OpenRootHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|rsServices
argument_list|,
name|regionInfo
argument_list|,
name|htd
argument_list|,
name|EventType
operator|.
name|M_RS_OPEN_ROOT
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OpenRootHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
name|int
name|versionOfOfflineNode
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|rsServices
argument_list|,
name|regionInfo
argument_list|,
name|htd
argument_list|,
name|EventType
operator|.
name|M_RS_OPEN_ROOT
argument_list|,
name|versionOfOfflineNode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


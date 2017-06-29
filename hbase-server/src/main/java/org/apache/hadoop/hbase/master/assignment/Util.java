begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
operator|.
name|assignment
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
name|ServerName
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
name|ipc
operator|.
name|HBaseRpcController
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureEnv
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|AdminService
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoRequest
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoResponse
import|;
end_import

begin_comment
comment|/**  * Utility for this assignment package only.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|Util
block|{
specifier|private
name|Util
parameter_list|()
block|{}
comment|/**    * Raw call to remote regionserver to get info on a particular region.    * @throws IOException Let it out so can report this IOE as reason for failure    */
specifier|static
name|GetRegionInfoResponse
name|getRegionInfoResponse
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|regionLocation
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getRegionInfoResponse
argument_list|(
name|env
argument_list|,
name|regionLocation
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|static
name|GetRegionInfoResponse
name|getRegionInfoResponse
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|regionLocation
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
name|boolean
name|includeBestSplitRow
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: There is no timeout on this controller. Set one!
name|HBaseRpcController
name|controller
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getClusterConnection
argument_list|()
operator|.
name|getRpcControllerFactory
argument_list|()
operator|.
name|newController
argument_list|()
decl_stmt|;
specifier|final
name|AdminService
operator|.
name|BlockingInterface
name|admin
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getClusterConnection
argument_list|()
operator|.
name|getAdmin
argument_list|(
name|regionLocation
argument_list|)
decl_stmt|;
name|GetRegionInfoRequest
name|request
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|includeBestSplitRow
condition|)
block|{
name|request
operator|=
name|RequestConverter
operator|.
name|buildGetRegionInfoRequest
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|=
name|RequestConverter
operator|.
name|buildGetRegionInfoRequest
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
return|return
name|admin
operator|.
name|getRegionInfo
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


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
name|rsgroup
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
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
name|Collections
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
name|CoprocessorEnvironment
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
name|coprocessor
operator|.
name|CoreCoprocessor
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
name|coprocessor
operator|.
name|HasMasterServices
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
name|coprocessor
operator|.
name|MasterCoprocessor
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
name|MasterServices
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
comment|/**  * @deprecated Keep it here only for compatibility with old client, all the logics have been moved  *             into core of HBase.  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|CoreCoprocessor
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSGroupAdminEndpoint
implements|implements
name|MasterCoprocessor
block|{
comment|// Only instance of RSGroupInfoManager. RSGroup aware load balancers ask for this instance on
comment|// their setup.
specifier|private
name|MasterServices
name|master
decl_stmt|;
specifier|private
name|RSGroupAdminServiceImpl
name|groupAdminService
init|=
operator|new
name|RSGroupAdminServiceImpl
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|env
operator|instanceof
name|HasMasterServices
operator|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Does not implement HMasterServices"
argument_list|)
throw|;
block|}
name|master
operator|=
operator|(
operator|(
name|HasMasterServices
operator|)
name|env
operator|)
operator|.
name|getMasterServices
argument_list|()
expr_stmt|;
name|groupAdminService
operator|.
name|initialize
argument_list|(
name|master
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|Service
argument_list|>
name|getServices
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|groupAdminService
argument_list|)
return|;
block|}
name|RSGroupInfoManager
name|getGroupInfoManager
parameter_list|()
block|{
return|return
name|master
operator|.
name|getRSGroupInfoManager
argument_list|()
return|;
block|}
block|}
end_class

end_unit


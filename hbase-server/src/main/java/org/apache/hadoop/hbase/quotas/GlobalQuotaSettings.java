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
name|HBaseInterfaceAudience
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
name|yetus
operator|.
name|audience
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|SetQuotaRequest
operator|.
name|Builder
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
name|QuotaProtos
operator|.
name|Quotas
import|;
end_import

begin_comment
comment|/**  * An object which captures all quotas types (throttle or space) for a subject (user, table, or  * namespace). This is used inside of the HBase RegionServer to act as an analogy to the  * ProtocolBuffer class {@link Quotas}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|GlobalQuotaSettings
extends|extends
name|QuotaSettings
block|{
specifier|protected
name|GlobalQuotaSettings
parameter_list|(
name|String
name|userName
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|namespace
parameter_list|,
name|String
name|regionServer
parameter_list|)
block|{
name|super
argument_list|(
name|userName
argument_list|,
name|tableName
argument_list|,
name|namespace
argument_list|,
name|regionServer
argument_list|)
expr_stmt|;
block|}
comment|/**    * Computes a list of QuotaSettings that present the complete quota state of the combination of    * this user, table, and/or namespace. Beware in calling this method repeatedly as the    * implementation of it may be costly.    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|QuotaSettings
argument_list|>
name|getQuotaSettings
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|QuotaType
name|getQuotaType
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setupSetQuotaRequest
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
comment|// ThrottleSettings should be used instead for setting a throttle quota.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This class should not be used to generate a SetQuotaRequest."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit


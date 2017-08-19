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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|client
operator|.
name|Connection
import|;
end_import

begin_comment
comment|/**  * A SpaceQuotaSnapshotNotifier implementation for testing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SpaceQuotaSnapshotNotifierForTest
implements|implements
name|SpaceQuotaSnapshotNotifier
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SpaceQuotaSnapshotNotifierForTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|tableQuotaSnapshots
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Connection
name|conn
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|transitionTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|SpaceQuotaSnapshot
name|snapshot
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Persisting "
operator|+
name|tableName
operator|+
literal|"=>"
operator|+
name|snapshot
argument_list|)
expr_stmt|;
block|}
name|tableQuotaSnapshots
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|TableName
argument_list|,
name|SpaceQuotaSnapshot
argument_list|>
name|copySnapshots
parameter_list|()
block|{
return|return
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|this
operator|.
name|tableQuotaSnapshots
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|clearSnapshots
parameter_list|()
block|{
name|this
operator|.
name|tableQuotaSnapshots
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


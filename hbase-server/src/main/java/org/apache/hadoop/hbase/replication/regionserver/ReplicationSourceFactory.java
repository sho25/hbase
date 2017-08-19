begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
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
name|conf
operator|.
name|Configuration
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
name|replication
operator|.
name|ReplicationQueueInfo
import|;
end_import

begin_comment
comment|/**  * Constructs a {@link ReplicationSourceInterface}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationSourceFactory
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
name|ReplicationSourceFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|ReplicationSourceInterface
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|peerId
parameter_list|)
block|{
name|ReplicationQueueInfo
name|replicationQueueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
name|boolean
name|isQueueRecovered
init|=
name|replicationQueueInfo
operator|.
name|isQueueRecovered
argument_list|()
decl_stmt|;
name|ReplicationSourceInterface
name|src
decl_stmt|;
try|try
block|{
name|String
name|defaultReplicationSourceImpl
init|=
name|isQueueRecovered
condition|?
name|RecoveredReplicationSource
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
else|:
name|ReplicationSource
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"replication.replicationsource.implementation"
argument_list|,
name|defaultReplicationSourceImpl
argument_list|)
argument_list|)
decl_stmt|;
name|src
operator|=
operator|(
name|ReplicationSourceInterface
operator|)
name|c
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Passed replication source implementation throws errors, "
operator|+
literal|"defaulting to ReplicationSource"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|src
operator|=
name|isQueueRecovered
condition|?
operator|new
name|RecoveredReplicationSource
argument_list|()
else|:
operator|new
name|ReplicationSource
argument_list|()
expr_stmt|;
block|}
return|return
name|src
return|;
block|}
block|}
end_class

end_unit


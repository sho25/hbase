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
name|List
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
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|HConstants
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
name|ObserverContext
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
name|RegionCoprocessorEnvironment
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
name|RegionObserver
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
name|HRegionServer
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
name|util
operator|.
name|Pair
import|;
end_import

begin_comment
comment|/**  * An Observer to facilitate replication operations  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|ReplicationObserver
implements|implements
name|RegionObserver
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
name|ReplicationObserver
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preCommitStoreFile
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|pairs
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionCoprocessorEnvironment
name|env
init|=
name|ctx
operator|.
name|getEnvironment
argument_list|()
decl_stmt|;
name|Configuration
name|c
init|=
name|env
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
name|pairs
operator|==
literal|null
operator|||
name|pairs
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|c
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_BULKLOAD_ENABLE_DEFAULT
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping recording bulk load entries in preCommitStoreFile for bulkloaded "
operator|+
literal|"data replication."
argument_list|)
expr_stmt|;
return|return;
block|}
name|HRegionServer
name|rs
init|=
operator|(
name|HRegionServer
operator|)
name|env
operator|.
name|getRegionServerServices
argument_list|()
decl_stmt|;
name|Replication
name|rep
init|=
operator|(
name|Replication
operator|)
name|rs
operator|.
name|getReplicationSourceService
argument_list|()
decl_stmt|;
name|rep
operator|.
name|addHFileRefsToQueue
argument_list|(
name|env
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|,
name|family
argument_list|,
name|pairs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


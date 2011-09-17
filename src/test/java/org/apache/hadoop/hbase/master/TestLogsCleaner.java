begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|FileStatus
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
name|FileSystem
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
name|HBaseTestingUtility
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
name|catalog
operator|.
name|CatalogTracker
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
name|ReplicationZookeeper
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
name|regionserver
operator|.
name|Replication
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestLogsCleaner
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLogCleaning
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Replication
operator|.
name|decorateMasterConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|()
decl_stmt|;
name|ReplicationZookeeper
name|zkHelper
init|=
operator|new
name|ReplicationZookeeper
argument_list|(
name|server
argument_list|,
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|oldLogDir
init|=
operator|new
name|Path
argument_list|(
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|)
decl_stmt|;
name|String
name|fakeMachineName
init|=
name|URLEncoder
operator|.
name|encode
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"UTF8"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LogCleaner
name|cleaner
init|=
operator|new
name|LogCleaner
argument_list|(
literal|1000
argument_list|,
name|server
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|oldLogDir
argument_list|)
decl_stmt|;
comment|// Create 2 invalid files, 1 "recent" file, 1 very new file and 30 old files
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|oldLogDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|oldLogDir
argument_list|)
expr_stmt|;
comment|// Case 1: 2 invalid files, which would be deleted directly
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Case 2: 1 "recent" file, not even deletable for the first log cleaner
comment|// (TimeToLiveLogCleaner), so we are not going down the chain
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
name|now
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Now is: "
operator|+
name|now
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
comment|// Case 3: old files which would be deletable for the first log cleaner
comment|// (TimeToLiveLogCleaner), and also for the second (ReplicationLogCleaner)
name|Path
name|fileName
init|=
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
operator|(
name|now
operator|-
literal|6000000
operator|-
name|i
operator|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
comment|// Case 4: put 3 old log files in ZK indicating that they are scheduled
comment|// for replication so these files would pass the first log cleaner
comment|// (TimeToLiveLogCleaner) but would be rejected by the second
comment|// (ReplicationLogCleaner)
if|if
condition|(
name|i
operator|%
operator|(
literal|30
operator|/
literal|3
operator|)
operator|==
literal|0
condition|)
block|{
name|zkHelper
operator|.
name|addLogToList
argument_list|(
name|fileName
operator|.
name|getName
argument_list|()
argument_list|,
name|fakeMachineName
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Replication log file: "
operator|+
name|fileName
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|FileStatus
name|stat
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|stat
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Case 2: 1 newer file, not even deletable for the first log cleaner
comment|// (TimeToLiveLogCleaner), so we are not going down the chain
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|oldLogDir
argument_list|,
name|fakeMachineName
operator|+
literal|"."
operator|+
operator|(
name|now
operator|+
literal|10000
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|34
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
comment|// We end up with the current log file, a newer one and the 3 old log
comment|// files which are scheduled for replication
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|oldLogDir
argument_list|)
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Kept log files: "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|DummyServer
implements|implements
name|Server
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
try|try
block|{
return|return
operator|new
name|ZooKeeperWatcher
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
literal|"dummy server"
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
operator|new
name|ServerName
argument_list|(
literal|"regionserver,60020,000000"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit


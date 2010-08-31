begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
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
name|master
operator|.
name|HMaster
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
name|LogCleanerDelegate
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

begin_comment
comment|// REENALBE import org.apache.hadoop.hbase.zookeeper.ZooKeeperWrapper;
end_comment

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|Watcher
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
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

begin_comment
comment|/**  * Implementation of a log cleaner that checks if a log is still scheduled for  * replication before deleting it when its TTL is over.  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationLogCleaner
implements|implements
name|LogCleanerDelegate
implements|,
name|Watcher
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
name|ReplicationLogCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ReplicationZookeeper
name|zkHelper
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|hlogs
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Instantiates the cleaner, does nothing more.    */
specifier|public
name|ReplicationLogCleaner
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isLogDeletable
parameter_list|(
name|Path
name|filePath
parameter_list|)
block|{
name|String
name|log
init|=
name|filePath
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// If we saw the hlog previously, let's consider it's still used
comment|// At some point in the future we will refresh the list and it will be gone
if|if
condition|(
name|this
operator|.
name|hlogs
operator|.
name|contains
argument_list|(
name|log
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Let's see it's still there
comment|// This solution makes every miss very expensive to process since we
comment|// almost completely refresh the cache each time
return|return
operator|!
name|refreshHLogsAndSearch
argument_list|(
name|log
argument_list|)
return|;
block|}
comment|/**    * Search through all the hlogs we have in ZK to refresh the cache    * If a log is specified and found, then we early out and return true    * @param searchedLog log we are searching for, pass null to cache everything    *                    that's in zookeeper.    * @return false until a specified log is found.    */
specifier|private
name|boolean
name|refreshHLogsAndSearch
parameter_list|(
name|String
name|searchedLog
parameter_list|)
block|{
name|this
operator|.
name|hlogs
operator|.
name|clear
argument_list|()
expr_stmt|;
specifier|final
name|boolean
name|lookForLog
init|=
name|searchedLog
operator|!=
literal|null
decl_stmt|;
comment|// REENALBE
comment|//    List<String> rss = zkHelper.getListOfReplicators(this);
comment|//    if (rss == null) {
comment|//      LOG.debug("Didn't find any region server that replicates, deleting: " +
comment|//          searchedLog);
comment|//      return false;
comment|//    }
comment|//    for (String rs: rss) {
comment|//      List<String> listOfPeers = zkHelper.getListPeersForRS(rs, this);
comment|//      // if rs just died, this will be null
comment|//      if (listOfPeers == null) {
comment|//        continue;
comment|//      }
comment|//      for (String id : listOfPeers) {
comment|//        List<String> peersHlogs = zkHelper.getListHLogsForPeerForRS(rs, id, this);
comment|//        if (peersHlogs != null) {
comment|//          this.hlogs.addAll(peersHlogs);
comment|//        }
comment|//        // early exit if we found the log
comment|//        if(lookForLog&& this.hlogs.contains(searchedLog)) {
comment|//          LOG.debug("Found log in ZK, keeping: " + searchedLog);
comment|//          return true;
comment|//        }
comment|//      }
comment|//    }
name|LOG
operator|.
name|debug
argument_list|(
literal|"Didn't find this log in ZK, deleting: "
operator|+
name|searchedLog
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|//    try {
comment|// REENABLE
comment|//      this.zkHelper = new ReplicationZookeeperWrapper(
comment|//          ZooKeeperWrapper.createInstance(this.conf,
comment|//              HMaster.class.getName()),
comment|//          this.conf, new AtomicBoolean(true), null);
comment|//    } catch (IOException e) {
comment|//      LOG.error(e);
comment|//    }
name|refreshHLogsAndSearch
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|WatchedEvent
name|watchedEvent
parameter_list|)
block|{}
block|}
end_class

end_unit


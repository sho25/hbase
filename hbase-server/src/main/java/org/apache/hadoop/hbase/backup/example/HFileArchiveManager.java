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
name|backup
operator|.
name|example
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
name|hbase
operator|.
name|ZooKeeperConnectionException
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
name|Bytes
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
name|ZKUtil
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
name|ZKWatcher
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
name|ZNodePaths
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Client-side manager for which table's hfiles should be preserved for long-term archive.  * @see ZKTableArchiveClient  * @see HFileArchiveTableMonitor  * @see LongTermArchivingHFileCleaner  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|HFileArchiveManager
block|{
specifier|private
specifier|final
name|String
name|archiveZnode
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HFileArchiveManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ZKWatcher
name|zooKeeper
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|public
name|HFileArchiveManager
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|this
operator|.
name|zooKeeper
operator|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"hfileArchiveManager-on-"
operator|+
name|connection
operator|.
name|toString
argument_list|()
argument_list|,
name|connection
argument_list|)
expr_stmt|;
name|this
operator|.
name|archiveZnode
operator|=
name|ZKTableArchiveClient
operator|.
name|getArchiveZNode
argument_list|(
name|this
operator|.
name|zooKeeper
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|zooKeeper
argument_list|)
expr_stmt|;
block|}
comment|/**    * Turn on auto-backups of HFiles on the specified table.    *<p>    * When HFiles would be deleted from the hfile archive, they are instead preserved.    * @param table name of the table for which to preserve hfiles.    * @return<tt>this</tt> for chaining.    * @throws KeeperException if we can't reach zookeeper to update the hfile cleaner.    */
specifier|public
name|HFileArchiveManager
name|enableHFileBackup
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
throws|throws
name|KeeperException
block|{
name|enable
argument_list|(
name|this
operator|.
name|zooKeeper
argument_list|,
name|table
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Stop retaining HFiles for the given table in the archive. HFiles will be cleaned up on the next    * pass of the {@link org.apache.hadoop.hbase.master.cleaner.HFileCleaner}, if the HFiles are    * retained by another cleaner.    * @param table name of the table for which to disable hfile retention.    * @return<tt>this</tt> for chaining.    * @throws KeeperException if if we can't reach zookeeper to update the hfile cleaner.    */
specifier|public
name|HFileArchiveManager
name|disableHFileBackup
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
throws|throws
name|KeeperException
block|{
name|disable
argument_list|(
name|this
operator|.
name|zooKeeper
argument_list|,
name|table
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Disable long-term archival of all hfiles for all tables in the cluster.    * @return<tt>this</tt> for chaining.    * @throws IOException if the number of attempts is exceeded    */
specifier|public
name|HFileArchiveManager
name|disableHFileBackup
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Disabling backups on all tables."
argument_list|)
expr_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|this
operator|.
name|zooKeeper
argument_list|,
name|archiveZnode
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected ZK exception!"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Perform a best effort enable of hfile retention, which relies on zookeeper communicating the    * change back to the hfile cleaner.    *<p>    * No attempt is made to make sure that backups are successfully created - it is inherently an    *<b>asynchronous operation</b>.    * @param zooKeeper watcher connection to zk cluster    * @param table table name on which to enable archiving    * @throws KeeperException if a ZooKeeper operation fails    */
specifier|private
name|void
name|enable
parameter_list|(
name|ZKWatcher
name|zooKeeper
parameter_list|,
name|byte
index|[]
name|table
parameter_list|)
throws|throws
name|KeeperException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ensuring archiving znode exists"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createAndFailSilent
argument_list|(
name|zooKeeper
argument_list|,
name|archiveZnode
argument_list|)
expr_stmt|;
comment|// then add the table to the list of znodes to archive
name|String
name|tableNode
init|=
name|this
operator|.
name|getTableNode
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating: "
operator|+
name|tableNode
operator|+
literal|", data: []"
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|createSetData
argument_list|(
name|zooKeeper
argument_list|,
name|tableNode
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * Disable all archiving of files for a given table    *<p>    * Inherently an<b>asynchronous operation</b>.    * @param zooKeeper watcher for the ZK cluster    * @param table name of the table to disable    * @throws KeeperException if an unexpected ZK connection issues occurs    */
specifier|private
name|void
name|disable
parameter_list|(
name|ZKWatcher
name|zooKeeper
parameter_list|,
name|byte
index|[]
name|table
parameter_list|)
throws|throws
name|KeeperException
block|{
comment|// ensure the latest state of the archive node is found
name|zooKeeper
operator|.
name|sync
argument_list|(
name|archiveZnode
argument_list|)
expr_stmt|;
comment|// if the top-level archive node is gone, then we are done
if|if
condition|(
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zooKeeper
argument_list|,
name|archiveZnode
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return;
block|}
comment|// delete the table node, from the archive
name|String
name|tableNode
init|=
name|this
operator|.
name|getTableNode
argument_list|(
name|table
argument_list|)
decl_stmt|;
comment|// make sure the table is the latest version so the delete takes
name|zooKeeper
operator|.
name|sync
argument_list|(
name|tableNode
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempting to delete table node:"
operator|+
name|tableNode
argument_list|)
expr_stmt|;
name|ZKUtil
operator|.
name|deleteNodeRecursively
argument_list|(
name|zooKeeper
argument_list|,
name|tableNode
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|stopped
condition|)
block|{
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stopping HFileArchiveManager..."
argument_list|)
expr_stmt|;
name|this
operator|.
name|zooKeeper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Check to see if the table is currently marked for archiving    * @param table name of the table to check    * @return<tt>true</tt> if the archive znode for that table exists,<tt>false</tt> if not    * @throws KeeperException if an unexpected zookeeper error occurs    */
specifier|public
name|boolean
name|isArchivingEnabled
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
throws|throws
name|KeeperException
block|{
name|String
name|tableNode
init|=
name|this
operator|.
name|getTableNode
argument_list|(
name|table
argument_list|)
decl_stmt|;
return|return
name|ZKUtil
operator|.
name|checkExists
argument_list|(
name|zooKeeper
argument_list|,
name|tableNode
argument_list|)
operator|>=
literal|0
return|;
block|}
comment|/**    * Get the zookeeper node associated with archiving the given table    * @param table name of the table to check    * @return znode for the table's archive status    */
specifier|private
name|String
name|getTableNode
parameter_list|(
name|byte
index|[]
name|table
parameter_list|)
block|{
return|return
name|ZNodePaths
operator|.
name|joinZNode
argument_list|(
name|archiveZnode
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|table
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit


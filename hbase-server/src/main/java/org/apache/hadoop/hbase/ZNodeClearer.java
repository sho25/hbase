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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
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
name|hbase
operator|.
name|master
operator|.
name|balancer
operator|.
name|BaseLoadBalancer
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
name|MasterAddressTracker
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
name|ZooKeeperWatcher
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

begin_comment
comment|/**  *<p>Contains a set of methods for the collaboration between the start/stop scripts and the  * servers. It allows to delete immediately the znode when the master or the regions server crashes.  * The region server / master writes a specific file when it starts / becomes main master. When they  * end properly, they delete the file.</p>  *<p>In the script, we check for the existence of these files when the program ends. If they still  * exist we conclude that the server crashed, likely without deleting their znode. To have a faster  * recovery we delete immediately the znode.</p>  *<p>The strategy depends on the server type. For a region server we store the znode path in the  * file, and use it to delete it. for a master, as the znode path constant whatever the server, we  * check its content to make sure that the backup server is not now in charge.</p>  */
end_comment

begin_class
specifier|public
class|class
name|ZNodeClearer
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
name|ZNodeClearer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ZNodeClearer
parameter_list|()
block|{}
comment|/**    * Logs the errors without failing on exception.    */
specifier|public
specifier|static
name|void
name|writeMyEphemeralNodeOnDisk
parameter_list|(
name|String
name|fileContent
parameter_list|)
block|{
name|String
name|fileName
init|=
name|ZNodeClearer
operator|.
name|getMyEphemeralNodeFileName
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileName
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Environment variable HBASE_ZNODE_FILE not set; znodes will not be cleared "
operator|+
literal|"on crash by start scripts (Longer MTTR!)"
argument_list|)
expr_stmt|;
return|return;
block|}
name|FileWriter
name|fstream
decl_stmt|;
try|try
block|{
name|fstream
operator|=
operator|new
name|FileWriter
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't write znode file "
operator|+
name|fileName
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|BufferedWriter
name|out
init|=
operator|new
name|BufferedWriter
argument_list|(
name|fstream
argument_list|)
decl_stmt|;
try|try
block|{
try|try
block|{
name|out
operator|.
name|write
argument_list|(
name|fileContent
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|fstream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't write znode file "
operator|+
name|fileName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * read the content of znode file, expects a single line.    */
specifier|public
specifier|static
name|String
name|readMyEphemeralNodeOnDisk
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|fileName
init|=
name|getMyEphemeralNodeFileName
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"No filename; set environment variable HBASE_ZNODE_FILE"
argument_list|)
throw|;
block|}
name|FileReader
name|znodeFile
init|=
operator|new
name|FileReader
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
name|BufferedReader
name|br
init|=
literal|null
decl_stmt|;
try|try
block|{
name|br
operator|=
operator|new
name|BufferedReader
argument_list|(
name|znodeFile
argument_list|)
expr_stmt|;
name|String
name|file_content
init|=
name|br
operator|.
name|readLine
argument_list|()
decl_stmt|;
return|return
name|file_content
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|br
operator|!=
literal|null
condition|)
name|br
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Get the name of the file used to store the znode contents    */
specifier|public
specifier|static
name|String
name|getMyEphemeralNodeFileName
parameter_list|()
block|{
return|return
name|System
operator|.
name|getenv
argument_list|()
operator|.
name|get
argument_list|(
literal|"HBASE_ZNODE_FILE"
argument_list|)
return|;
block|}
comment|/**    *  delete the znode file    */
specifier|public
specifier|static
name|void
name|deleteMyEphemeralNodeOnDisk
parameter_list|()
block|{
name|String
name|fileName
init|=
name|getMyEphemeralNodeFileName
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileName
operator|!=
literal|null
condition|)
block|{
operator|new
name|File
argument_list|(
name|fileName
argument_list|)
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * See HBASE-14861. We are extracting master ServerName from rsZnodePath    * example: "/hbase/rs/server.example.com,16020,1448266496481"    * @param rsZnodePath from HBASE_ZNODE_FILE    * @return String representation of ServerName or null if fails    */
specifier|public
specifier|static
name|String
name|parseMasterServerName
parameter_list|(
name|String
name|rsZnodePath
parameter_list|)
block|{
name|String
name|masterServerName
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
index|[]
name|rsZnodeParts
init|=
name|rsZnodePath
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|masterServerName
operator|=
name|rsZnodeParts
index|[
name|rsZnodeParts
operator|.
name|length
operator|-
literal|1
index|]
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"String "
operator|+
name|rsZnodePath
operator|+
literal|" has wrong format"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|masterServerName
return|;
block|}
comment|/**    *     * @return true if cluster is configured with master-rs collocation     */
specifier|private
specifier|static
name|boolean
name|tablesOnMaster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|boolean
name|tablesOnMaster
init|=
literal|true
decl_stmt|;
name|String
name|confValue
init|=
name|conf
operator|.
name|get
argument_list|(
name|BaseLoadBalancer
operator|.
name|TABLES_ON_MASTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|confValue
operator|!=
literal|null
operator|&&
name|confValue
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"none"
argument_list|)
condition|)
block|{
name|tablesOnMaster
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|tablesOnMaster
return|;
block|}
comment|/**    * Delete the master znode if its content (ServerName string) is the same    *  as the one in the znode file. (env: HBASE_ZNODE_FILE). I case of master-rs    *  colloaction we extract ServerName string from rsZnode path.(HBASE-14861)    * @return true on successful deletion, false otherwise.    */
specifier|public
specifier|static
name|boolean
name|clear
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Configuration
name|tempConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|tempConf
operator|.
name|setInt
argument_list|(
literal|"zookeeper.recovery.retry"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|ZooKeeperWatcher
name|zkw
decl_stmt|;
try|try
block|{
name|zkw
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|tempConf
argument_list|,
literal|"clean znode for master"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
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
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't connect to zookeeper to read the master znode"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|String
name|znodeFileContent
decl_stmt|;
try|try
block|{
name|znodeFileContent
operator|=
name|ZNodeClearer
operator|.
name|readMyEphemeralNodeOnDisk
argument_list|()
expr_stmt|;
if|if
condition|(
name|ZNodeClearer
operator|.
name|tablesOnMaster
argument_list|(
name|conf
argument_list|)
condition|)
block|{
comment|//In case of master crash also remove rsZnode since master is also regionserver
name|ZKUtil
operator|.
name|deleteNodeFailSilent
argument_list|(
name|zkw
argument_list|,
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|zkw
operator|.
name|znodePaths
operator|.
name|rsZNode
argument_list|,
name|znodeFileContent
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|MasterAddressTracker
operator|.
name|deleteIfEquals
argument_list|(
name|zkw
argument_list|,
name|ZNodeClearer
operator|.
name|parseMasterServerName
argument_list|(
name|znodeFileContent
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|MasterAddressTracker
operator|.
name|deleteIfEquals
argument_list|(
name|zkw
argument_list|,
name|znodeFileContent
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
comment|// If no file, just keep going -- return success.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't find the znode file; presume non-fatal"
argument_list|,
name|fnfe
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't read the content of the znode file"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"ZooKeeper exception deleting znode"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
finally|finally
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


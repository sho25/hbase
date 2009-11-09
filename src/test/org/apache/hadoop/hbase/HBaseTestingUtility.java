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
package|;
end_package

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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|UUID
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
name|commons
operator|.
name|logging
operator|.
name|impl
operator|.
name|Jdk14Logger
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
name|impl
operator|.
name|Log4JLogger
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
name|client
operator|.
name|Delete
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
name|HBaseAdmin
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
name|HConnection
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
name|HConnectionManager
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
name|HTable
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
name|Put
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|util
operator|.
name|FSUtils
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
name|Writables
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|mapred
operator|.
name|MiniMRCluster
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|corba
operator|.
name|se
operator|.
name|pept
operator|.
name|transport
operator|.
name|Connection
import|;
end_import

begin_comment
comment|/**  * Facility for testing HBase. Added as tool to abet junit4 testing.  Replaces  * old HBaseTestCase and HBaseCluserTestCase functionality.  * Create an instance and keep it around doing HBase testing.  This class is  * meant to be your one-stop shop for anything you mind need testing.  Manages  * one cluster at a time only.  Depends on log4j on classpath and hbase-site.xml  * for logging and test-run configuration.  It does not set logging levels nor  * make changes to configuration parameters.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseTestingUtility
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
specifier|private
name|MiniZooKeeperCluster
name|zkCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|dfsCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|hbaseCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|MiniMRCluster
name|mrCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|File
name|clusterTestBuildDir
init|=
literal|null
decl_stmt|;
comment|/** System property key to get test directory value.    */
specifier|public
specifier|static
specifier|final
name|String
name|TEST_DIRECTORY_KEY
init|=
literal|"test.build.data"
decl_stmt|;
comment|/**    * @return Instance of HBaseConfiguration.    */
specifier|public
name|HBaseConfiguration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
comment|/**    * @return Where to write test data on local filesystem; usually build/test/data    */
specifier|public
name|Path
name|getTestDir
parameter_list|()
block|{
return|return
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
name|TEST_DIRECTORY_KEY
argument_list|,
literal|"build/test/data"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @param subdirName    * @return Path to a subdirectory named<code>subdirName</code> under    * {@link #getTestDir()}.    */
specifier|public
name|Path
name|getTestDir
parameter_list|(
specifier|final
name|String
name|subdirName
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|getTestDir
argument_list|()
argument_list|,
name|subdirName
argument_list|)
return|;
block|}
comment|/**    * Start up a minicluster of hbase, dfs, and zookeeper.    * @throws Exception     */
specifier|public
name|void
name|startMiniCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Start up a minicluster of hbase, optinally dfs, and zookeeper.    * Modifies Configuration.  Homes the cluster data directory under a random    * subdirectory in a directory under System property test.build.data.    * @param servers Number of servers to start up.  We'll start this many    * datanodes and regionservers.  If servers is> 1, then make sure    * hbase.regionserver.info.port is -1 (i.e. no ui per regionserver) otherwise    * bind errors.    * @throws Exception    * @see {@link #shutdownMiniCluster()}    */
specifier|public
name|void
name|startMiniCluster
parameter_list|(
specifier|final
name|int
name|servers
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting up minicluster"
argument_list|)
expr_stmt|;
comment|// If we already put up a cluster, fail.
if|if
condition|(
name|this
operator|.
name|clusterTestBuildDir
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cluster already running at "
operator|+
name|this
operator|.
name|clusterTestBuildDir
argument_list|)
throw|;
block|}
comment|// Now, home our cluster in a dir under build/test.  Give it a random name
comment|// so can have many concurrent clusters running if we need to.  Need to
comment|// amend the test.build.data System property.  Its what minidfscluster bases
comment|// it data dir on.  Moding a System property is not the way to do concurrent
comment|// instances -- another instance could grab the temporary
comment|// value unintentionally -- but not anything can do about it at moment; its
comment|// how the minidfscluster works.
name|String
name|oldTestBuildDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|TEST_DIRECTORY_KEY
argument_list|,
literal|"build/test/data"
argument_list|)
decl_stmt|;
name|String
name|randomStr
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|clusterTestBuildDirStr
init|=
name|oldTestBuildDir
operator|+
literal|"."
operator|+
name|randomStr
decl_stmt|;
name|this
operator|.
name|clusterTestBuildDir
operator|=
operator|new
name|File
argument_list|(
name|clusterTestBuildDirStr
argument_list|)
operator|.
name|getAbsoluteFile
argument_list|()
expr_stmt|;
comment|// Have it cleaned up on exit
name|this
operator|.
name|clusterTestBuildDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
comment|// Set our random dir while minidfscluster is being constructed.
name|System
operator|.
name|setProperty
argument_list|(
name|TEST_DIRECTORY_KEY
argument_list|,
name|clusterTestBuildDirStr
argument_list|)
expr_stmt|;
comment|// Bring up mini dfs cluster. This spews a bunch of warnings about missing
comment|// scheme. TODO: fix.
comment|// Complaints are 'Scheme is undefined for build/test/data/dfs/name1'.
name|this
operator|.
name|dfsCluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
literal|0
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|servers
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Restore System property. minidfscluster accesses content of
comment|// the TEST_DIRECTORY_KEY to make bad blocks, a feature we are not using,
comment|// but otherwise, just in constructor.
name|System
operator|.
name|setProperty
argument_list|(
name|TEST_DIRECTORY_KEY
argument_list|,
name|oldTestBuildDir
argument_list|)
expr_stmt|;
comment|// Mangle conf so fs parameter points to minidfs we just started up
name|FileSystem
name|fs
init|=
name|this
operator|.
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|dfsCluster
operator|.
name|waitClusterUp
argument_list|()
expr_stmt|;
comment|// Note that this is done before we create the MiniHBaseCluster because we
comment|// need to edit the config to add the ZooKeeper servers.
name|this
operator|.
name|zkCluster
operator|=
operator|new
name|MiniZooKeeperCluster
argument_list|()
expr_stmt|;
name|int
name|clientPort
init|=
name|this
operator|.
name|zkCluster
operator|.
name|startup
argument_list|(
name|this
operator|.
name|clusterTestBuildDir
argument_list|)
decl_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|clientPort
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now do the mini hbase cluster.  Set the hbase.rootdir in config.
name|Path
name|hbaseRootdir
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
name|fs
operator|.
name|getHomeDirectory
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|hbaseRootdir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|hbaseRootdir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|hbaseRootdir
argument_list|)
expr_stmt|;
name|this
operator|.
name|hbaseCluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|servers
argument_list|)
expr_stmt|;
comment|// Don't leave here till we've done a successful scan of the .META.
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|s
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
continue|continue;
name|LOG
operator|.
name|info
argument_list|(
literal|"Minicluster is up"
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws IOException    * @see {@link #startMiniCluster(boolean, int)}    */
specifier|public
name|void
name|shutdownMiniCluster
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down minicluster"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|hbaseCluster
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|hbaseCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
comment|// Wait till hbase is down before going on to shutdown zk.
name|this
operator|.
name|hbaseCluster
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|zkCluster
operator|!=
literal|null
condition|)
name|this
operator|.
name|zkCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|dfsCluster
operator|!=
literal|null
condition|)
block|{
comment|// The below throws an exception per dn, AsynchronousCloseException.
name|this
operator|.
name|dfsCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|// Clean up our directory.
if|if
condition|(
name|this
operator|.
name|clusterTestBuildDir
operator|!=
literal|null
operator|&&
name|this
operator|.
name|clusterTestBuildDir
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// Need to use deleteDirectory because File.delete required dir is empty.
if|if
condition|(
operator|!
name|FSUtils
operator|.
name|deleteDirectory
argument_list|(
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|this
operator|.
name|clusterTestBuildDir
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed delete of "
operator|+
name|this
operator|.
name|clusterTestBuildDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Minicluster is down"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Flushes all caches in the mini hbase cluster    * @throws IOException    */
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|hbaseCluster
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create a table.    * @param tableName    * @param family    * @return An HTable instance for the created table.    * @throws IOException    */
specifier|public
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|family
block|}
argument_list|)
return|;
block|}
comment|/**    * Create a table.    * @param tableName    * @param families    * @return An HTable instance for the created table.    * @throws IOException    */
specifier|public
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
operator|(
operator|new
name|HBaseAdmin
argument_list|(
name|getConfiguration
argument_list|()
argument_list|)
operator|)
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
return|return
operator|new
name|HTable
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
return|;
block|}
comment|/**    * Create a table.    * @param tableName    * @param family    * @param numVersions    * @return An HTable instance for the created table.    * @throws IOException    */
specifier|public
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|int
name|numVersions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|family
block|}
argument_list|,
name|numVersions
argument_list|)
return|;
block|}
comment|/**    * Create a table.    * @param tableName    * @param families    * @param numVersions    * @return An HTable instance for the created table.    * @throws IOException    */
specifier|public
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|,
name|int
name|numVersions
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|,
name|numVersions
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_COMPRESSION
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_TTL
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
operator|(
operator|new
name|HBaseAdmin
argument_list|(
name|getConfiguration
argument_list|()
argument_list|)
operator|)
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
return|return
operator|new
name|HTable
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
return|;
block|}
comment|/**    * Create a table.    * @param tableName    * @param families    * @param numVersions    * @return An HTable instance for the created table.    * @throws IOException    */
specifier|public
name|HTable
name|createTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|,
name|int
index|[]
name|numVersions
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|,
name|numVersions
index|[
name|i
index|]
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_COMPRESSION
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_TTL
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
operator|(
operator|new
name|HBaseAdmin
argument_list|(
name|getConfiguration
argument_list|()
argument_list|)
operator|)
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
return|return
operator|new
name|HTable
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
return|;
block|}
comment|/**    * Load table with rows from 'aaa' to 'zzz'.    * @param t Table    * @param f Family    * @return Count of rows loaded.    * @throws IOException    */
specifier|public
name|int
name|loadTable
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|byte
index|[]
name|f
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|k
init|=
operator|new
name|byte
index|[
literal|3
index|]
decl_stmt|;
name|int
name|rowCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
name|b1
init|=
literal|'a'
init|;
name|b1
operator|<=
literal|'z'
condition|;
name|b1
operator|++
control|)
block|{
for|for
control|(
name|byte
name|b2
init|=
literal|'a'
init|;
name|b2
operator|<=
literal|'z'
condition|;
name|b2
operator|++
control|)
block|{
for|for
control|(
name|byte
name|b3
init|=
literal|'a'
init|;
name|b3
operator|<=
literal|'z'
condition|;
name|b3
operator|++
control|)
block|{
name|k
index|[
literal|0
index|]
operator|=
name|b1
expr_stmt|;
name|k
index|[
literal|1
index|]
operator|=
name|b2
expr_stmt|;
name|k
index|[
literal|2
index|]
operator|=
name|b3
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|f
argument_list|,
literal|null
argument_list|,
name|k
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|rowCount
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
name|rowCount
return|;
block|}
comment|/**    * Creates many regions names "aaa" to "zzz".    *     * @param table  The table to use for the data.    * @param columnFamily  The family to insert the data into.    * @throws IOException When creating the regions fails.    */
specifier|public
name|void
name|createMultiRegions
parameter_list|(
name|HTable
name|table
parameter_list|,
name|byte
index|[]
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|KEYS
init|=
block|{
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ggg"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hhh"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"iii"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"jjj"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"kkk"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"lll"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mmm"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"nnn"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ooo"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ppp"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qqq"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rrr"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"sss"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ttt"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"uuu"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"vvv"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"www"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xxx"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"yyy"
argument_list|)
block|}
decl_stmt|;
name|HBaseConfiguration
name|c
init|=
name|getConfiguration
argument_list|()
decl_stmt|;
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|c
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|table
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|htd
operator|.
name|hasFamily
argument_list|(
name|columnFamily
argument_list|)
condition|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|columnFamily
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
comment|// remove empty region - this is tricky as the mini cluster during the test
comment|// setup already has the "<tablename>,,123456789" row with an empty start
comment|// and end key. Adding the custom regions below adds those blindly,
comment|// including the new start region from empty to "bbb". lg
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rows
init|=
name|getMetaTableRows
argument_list|()
decl_stmt|;
comment|// add custom ones
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|KEYS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|j
init|=
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
name|KEYS
operator|.
name|length
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|table
operator|.
name|getTableDescriptor
argument_list|()
argument_list|,
name|KEYS
index|[
name|i
index|]
argument_list|,
name|KEYS
index|[
name|j
index|]
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
name|meta
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"createMultiRegions: inserted "
operator|+
name|hri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// see comment above, remove "old" (or previous) single region
for|for
control|(
name|byte
index|[]
name|row
range|:
name|rows
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"createMultiRegions: deleting meta row -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|meta
operator|.
name|delete
argument_list|(
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// flush cache of regions
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HConnection
name|conn
init|=
name|admin
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|conn
operator|.
name|clearRegionCache
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns all rows from the .META. table.    *    * @throws IOException When reading the rows fails.    */
specifier|public
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|getMetaTableRows
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|rows
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|ResultScanner
name|s
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|s
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"getMetaTableRows: row -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rows
operator|.
name|add
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|rows
return|;
block|}
comment|/**    * Removes all rows from the .META. in preparation to add custom ones.    *    * @throws IOException When removing the rows fails.    */
specifier|private
name|void
name|emptyMetaTable
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Delete
argument_list|>
name|deletes
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|()
decl_stmt|;
name|ResultScanner
name|s
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|s
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"emptyMetaTable: remove row -> "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|deletes
operator|.
name|add
argument_list|(
name|del
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|t
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Starts a<code>MiniMRCluster</code> with a default number of     *<code>TaskTracker</code>'s.    *    * @throws IOException When starting the cluster fails.    */
specifier|public
name|void
name|startMiniMapReduceCluster
parameter_list|()
throws|throws
name|IOException
block|{
name|startMiniMapReduceCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Starts a<code>MiniMRCluster</code>.    *    * @param servers  The number of<code>TaskTracker</code>'s to start.    * @throws IOException When starting the cluster fails.    */
specifier|public
name|void
name|startMiniMapReduceCluster
parameter_list|(
specifier|final
name|int
name|servers
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting mini mapreduce cluster..."
argument_list|)
expr_stmt|;
comment|// These are needed for the new and improved Map/Reduce framework
name|Configuration
name|c
init|=
name|getConfiguration
argument_list|()
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|,
name|c
operator|.
name|get
argument_list|(
literal|"hadoop.log.dir"
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"mapred.output.dir"
argument_list|,
name|c
operator|.
name|get
argument_list|(
literal|"hadoop.tmp.dir"
argument_list|)
argument_list|)
expr_stmt|;
name|mrCluster
operator|=
operator|new
name|MiniMRCluster
argument_list|(
name|servers
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mini mapreduce cluster started"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stops the previously started<code>MiniMRCluster</code>.     */
specifier|public
name|void
name|shutdownMiniMapReduceCluster
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping mini mapreduce cluster..."
argument_list|)
expr_stmt|;
if|if
condition|(
name|mrCluster
operator|!=
literal|null
condition|)
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Mini mapreduce cluster stopped"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Switches the logger for the given class to DEBUG level.    *    * @param clazz  The class for which to switch to debug logging.    */
specifier|public
name|void
name|enableDebug
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|Log
name|l
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|l
operator|instanceof
name|Log4JLogger
condition|)
block|{
operator|(
operator|(
name|Log4JLogger
operator|)
name|l
operator|)
operator|.
name|getLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|l
operator|instanceof
name|Jdk14Logger
condition|)
block|{
operator|(
operator|(
name|Jdk14Logger
operator|)
name|l
operator|)
operator|.
name|getLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Level
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|access
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|CategoryBasedTimeout
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
name|Coprocessor
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|hadoop
operator|.
name|hbase
operator|.
name|TableNotEnabledException
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
name|TableNotFoundException
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
name|Admin
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
name|client
operator|.
name|ConnectionFactory
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
name|Table
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
name|CoprocessorHost
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|SecurityTests
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
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_comment
comment|/**  * Performs coprocessor loads for variuos paths and malformed strings  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SecurityTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCoprocessorWhitelistMasterObserver
extends|extends
name|SecureTestUtil
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
name|TestCoprocessorWhitelistMasterObserver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TEST_TABLE
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testTable"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|tearDownTestCoprocessorWhitelistMasterObserver
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
try|try
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotEnabledException
name|ex
parameter_list|)
block|{
comment|// Table was left disabled by test
name|LOG
operator|.
name|info
argument_list|(
literal|"Table was left disabled by test"
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
name|ex
parameter_list|)
block|{
comment|// Table was not created for some reason?
name|LOG
operator|.
name|info
argument_list|(
literal|"Table was not created for some reason"
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|ClassRule
specifier|public
specifier|static
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|forClass
argument_list|(
name|TestCoprocessorWhitelistMasterObserver
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Test a table modification adding a coprocessor path    * which is not whitelisted    * @result An IOException should be thrown and caught    *         to show coprocessor is working as desired    * @param whitelistedPaths A String array of paths to add in    *         for the whitelisting configuration    * @param coprocessorPath A String to use as the    *         path for a mock coprocessor    */
specifier|private
specifier|static
name|void
name|positiveTestCase
parameter_list|(
name|String
index|[]
name|whitelistedPaths
parameter_list|,
name|String
name|coprocessorPath
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// load coprocessor under test
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CoprocessorWhitelistMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorWhitelistMasterObserver
operator|.
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
argument_list|,
name|whitelistedPaths
argument_list|)
expr_stmt|;
comment|// set retries low to raise exception quickly
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|TEST_FAMILY
block|}
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|t
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
literal|"net.clayb.hbase.coprocessor.NotWhitelisted"
argument_list|,
operator|new
name|Path
argument_list|(
name|coprocessorPath
argument_list|)
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Modifying Table"
argument_list|)
expr_stmt|;
try|try
block|{
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|modifyTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected coprocessor to raise IOException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// swallow exception from coprocessor
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Done Modifying Table"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|t
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is whitelisted    * @result The coprocessor should be added to the table    *         descriptor successfully    * @param whitelistedPaths A String array of paths to add in    *         for the whitelisting configuration    * @param coprocessorPath A String to use as the    *         path for a mock coprocessor    */
specifier|private
specifier|static
name|void
name|negativeTestCase
parameter_list|(
name|String
index|[]
name|whitelistedPaths
parameter_list|,
name|String
name|coprocessorPath
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// load coprocessor under test
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CoprocessorWhitelistMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// set retries low to raise exception quickly
comment|// set a coprocessor whitelist path for test
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorWhitelistMasterObserver
operator|.
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
argument_list|,
name|whitelistedPaths
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|TEST_FAMILY
block|}
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
comment|// disable table so we do not actually try loading non-existant
comment|// coprocessor file
name|admin
operator|.
name|disableTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|t
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
literal|"net.clayb.hbase.coprocessor.Whitelisted"
argument_list|,
operator|new
name|Path
argument_list|(
name|coprocessorPath
argument_list|)
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Modifying Table"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|t
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done Modifying Table"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is not whitelisted    * @result An IOException should be thrown and caught    *         to show coprocessor is working as desired    */
annotation|@
name|Test
specifier|public
name|void
name|testSubstringNonWhitelisted
parameter_list|()
throws|throws
name|Exception
block|{
name|positiveTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"/permitted/*"
block|}
argument_list|,
literal|"file:///notpermitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table creation including a coprocessor path    * which is not whitelisted    * @result Coprocessor should be added to table descriptor    *         Table is disabled to avoid an IOException due to    *         the added coprocessor not actually existing on disk    */
annotation|@
name|Test
specifier|public
name|void
name|testDifferentFileSystemNonWhitelisted
parameter_list|()
throws|throws
name|Exception
block|{
name|positiveTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"hdfs://foo/bar"
block|}
argument_list|,
literal|"file:///notpermitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is whitelisted    * @result Coprocessor should be added to table descriptor    *         Table is disabled to avoid an IOException due to    *         the added coprocessor not actually existing on disk    */
annotation|@
name|Test
specifier|public
name|void
name|testSchemeAndDirectorywhitelisted
parameter_list|()
throws|throws
name|Exception
block|{
name|negativeTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"/tmp"
block|,
literal|"file:///permitted/*"
block|}
argument_list|,
literal|"file:///permitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is whitelisted    * @result Coprocessor should be added to table descriptor    *         Table is disabled to avoid an IOException due to    *         the added coprocessor not actually existing on disk    */
annotation|@
name|Test
specifier|public
name|void
name|testSchemeWhitelisted
parameter_list|()
throws|throws
name|Exception
block|{
name|negativeTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"file:///"
block|}
argument_list|,
literal|"file:///permitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is whitelisted    * @result Coprocessor should be added to table descriptor    *         Table is disabled to avoid an IOException due to    *         the added coprocessor not actually existing on disk    */
annotation|@
name|Test
specifier|public
name|void
name|testDFSNameWhitelistedWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|negativeTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"hdfs://Your-FileSystem"
block|}
argument_list|,
literal|"hdfs://Your-FileSystem/permitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is whitelisted    * @result Coprocessor should be added to table descriptor    *         Table is disabled to avoid an IOException due to    *         the added coprocessor not actually existing on disk    */
annotation|@
name|Test
specifier|public
name|void
name|testDFSNameNotWhitelistedFails
parameter_list|()
throws|throws
name|Exception
block|{
name|positiveTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"hdfs://Your-FileSystem"
block|}
argument_list|,
literal|"hdfs://My-FileSystem/permitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table modification adding a coprocessor path    * which is whitelisted    * @result Coprocessor should be added to table descriptor    *         Table is disabled to avoid an IOException due to    *         the added coprocessor not actually existing on disk    */
annotation|@
name|Test
specifier|public
name|void
name|testBlanketWhitelist
parameter_list|()
throws|throws
name|Exception
block|{
name|negativeTestCase
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
argument_list|,
literal|"hdfs:///permitted/couldnotpossiblyexist.jar"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table creation including a coprocessor path    * which is not whitelisted    * @result Table will not be created due to the offending coprocessor    */
annotation|@
name|Test
specifier|public
name|void
name|testCreationNonWhitelistedCoprocessorPath
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// load coprocessor under test
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CoprocessorWhitelistMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorWhitelistMasterObserver
operator|.
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
comment|// set retries low to raise exception quickly
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
literal|"net.clayb.hbase.coprocessor.NotWhitelisted"
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file:///notpermitted/couldnotpossiblyexist.jar"
argument_list|)
argument_list|,
name|Coprocessor
operator|.
name|PRIORITY_USER
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating Table"
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected coprocessor to raise IOException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// swallow exception from coprocessor
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Done Creating Table"
argument_list|)
expr_stmt|;
comment|// ensure table was not created
name|assertEquals
argument_list|(
operator|new
name|HTableDescriptor
index|[
literal|0
index|]
argument_list|,
name|admin
operator|.
name|listTables
argument_list|(
literal|"^"
operator|+
name|TEST_TABLE
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"$"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test a table creation including a coprocessor path    * which is on the classpath    * @result Table will be created with the coprocessor    */
annotation|@
name|Test
specifier|public
name|void
name|testCreationClasspathCoprocessor
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// load coprocessor under test
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CoprocessorWhitelistMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorWhitelistMasterObserver
operator|.
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
comment|// set retries low to raise exception quickly
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
literal|"org.apache.hadoop.hbase.coprocessor.BaseRegionObserver"
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating Table"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
comment|// ensure table was created and coprocessor is added to table
name|LOG
operator|.
name|info
argument_list|(
literal|"Done Creating Table"
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|t
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


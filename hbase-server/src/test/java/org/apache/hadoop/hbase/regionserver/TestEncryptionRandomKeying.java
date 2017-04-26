begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Key
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
name|RegionServerTests
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
name|io
operator|.
name|crypto
operator|.
name|Encryption
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
name|io
operator|.
name|crypto
operator|.
name|KeyProviderForTesting
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestEncryptionRandomKeying
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|Path
argument_list|>
name|findStorefilePaths
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Region
name|region
range|:
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|getOnlineRegions
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
control|)
block|{
for|for
control|(
name|Store
name|store
range|:
name|region
operator|.
name|getStores
argument_list|()
control|)
block|{
for|for
control|(
name|StoreFile
name|storefile
range|:
name|store
operator|.
name|getStorefiles
argument_list|()
control|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|storefile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|paths
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|extractHFileKey
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|Exception
block|{
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|path
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|Encryption
operator|.
name|Context
name|cryptoContext
init|=
name|reader
operator|.
name|getFileContext
argument_list|()
operator|.
name|getEncryptionContext
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Reader has a null crypto context"
argument_list|,
name|cryptoContext
argument_list|)
expr_stmt|;
name|Key
name|key
init|=
name|cryptoContext
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|key
operator|.
name|getEncoded
argument_list|()
return|;
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEYPROVIDER_CONF_KEY
argument_list|,
name|KeyProviderForTesting
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
comment|// Create the table schema
comment|// Specify an encryption algorithm without a key
name|htd
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"default"
argument_list|,
literal|"TestEncryptionRandomKeying"
argument_list|)
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
name|String
name|algorithm
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_KEY_ALGORITHM_CONF_KEY
argument_list|,
name|HConstants
operator|.
name|CIPHER_AES
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setEncryptionType
argument_list|(
name|algorithm
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
comment|// Start the minicluster
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// Create the test table
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
comment|// Create a store file
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomKeying
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Verify we have store file(s) with a random key
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|initialPaths
init|=
name|findStorefilePaths
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|initialPaths
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|initialPaths
control|)
block|{
name|assertNotNull
argument_list|(
literal|"Store file "
operator|+
name|path
operator|+
literal|" is not encrypted"
argument_list|,
name|extractHFileKey
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


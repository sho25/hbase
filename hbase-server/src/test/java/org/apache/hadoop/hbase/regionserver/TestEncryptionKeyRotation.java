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
name|assertNotNull
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
name|assertTrue
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
name|security
operator|.
name|Key
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
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
name|Collection
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
name|javax
operator|.
name|crypto
operator|.
name|spec
operator|.
name|SecretKeySpec
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
name|HBaseClassTestRule
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
name|Waiter
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
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|CompactionState
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
name|client
operator|.
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|crypto
operator|.
name|aes
operator|.
name|AES
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
name|security
operator|.
name|EncryptionUtil
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
name|security
operator|.
name|User
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|TestName
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
name|TestEncryptionKeyRotation
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestEncryptionKeyRotation
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|final
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
specifier|final
name|Key
name|initialCFKey
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Key
name|secondCFKey
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
static|static
block|{
comment|// Create the test encryption keys
name|SecureRandom
name|rng
init|=
operator|new
name|SecureRandom
argument_list|()
decl_stmt|;
name|byte
index|[]
name|keyBytes
init|=
operator|new
name|byte
index|[
name|AES
operator|.
name|KEY_LENGTH
index|]
decl_stmt|;
name|rng
operator|.
name|nextBytes
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
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
name|initialCFKey
operator|=
operator|new
name|SecretKeySpec
argument_list|(
name|keyBytes
argument_list|,
name|algorithm
argument_list|)
expr_stmt|;
name|rng
operator|.
name|nextBytes
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
name|secondCFKey
operator|=
operator|new
name|SecretKeySpec
argument_list|(
name|keyBytes
argument_list|,
name|algorithm
argument_list|)
expr_stmt|;
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
comment|// Start the minicluster
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|testCFKeyRotation
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the table schema
name|TableDescriptorBuilder
name|tableDescriptorBuilder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"default"
argument_list|,
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
name|columnFamilyDescriptorBuilder
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
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
name|columnFamilyDescriptorBuilder
operator|.
name|setEncryptionType
argument_list|(
name|algorithm
argument_list|)
expr_stmt|;
name|columnFamilyDescriptorBuilder
operator|.
name|setEncryptionKey
argument_list|(
name|EncryptionUtil
operator|.
name|wrapKey
argument_list|(
name|conf
argument_list|,
literal|"hbase"
argument_list|,
name|initialCFKey
argument_list|)
argument_list|)
expr_stmt|;
name|tableDescriptorBuilder
operator|.
name|setColumnFamily
argument_list|(
name|columnFamilyDescriptorBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptor
name|tableDescriptor
init|=
name|tableDescriptorBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Create the table and some on disk files
name|createTableAndFlush
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
comment|// Verify we have store file(s) with the initial key
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|initialPaths
init|=
name|findStorefilePaths
argument_list|(
name|tableDescriptor
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
name|assertTrue
argument_list|(
literal|"Store file "
operator|+
name|path
operator|+
literal|" has incorrect key"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|initialCFKey
operator|.
name|getEncoded
argument_list|()
argument_list|,
name|extractHFileKey
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Update the schema with a new encryption key
name|columnFamilyDescriptorBuilder
operator|.
name|setEncryptionKey
argument_list|(
name|EncryptionUtil
operator|.
name|wrapKey
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
argument_list|,
name|secondCFKey
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|modifyColumnFamily
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|columnFamilyDescriptorBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// Need a predicate for online schema change
comment|// And major compact
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|majorCompact
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|// waiting for the major compaction to complete
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
name|Waiter
operator|.
name|Predicate
argument_list|<
name|IOException
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getCompactionState
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|==
name|CompactionState
operator|.
name|NONE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|pathsAfterCompaction
init|=
name|findStorefilePaths
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pathsAfterCompaction
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
name|pathsAfterCompaction
control|)
block|{
name|assertTrue
argument_list|(
literal|"Store file "
operator|+
name|path
operator|+
literal|" has incorrect key"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|secondCFKey
operator|.
name|getEncoded
argument_list|()
argument_list|,
name|extractHFileKey
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|compactedPaths
init|=
name|findCompactedStorefilePaths
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|compactedPaths
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
name|compactedPaths
control|)
block|{
name|assertTrue
argument_list|(
literal|"Store file "
operator|+
name|path
operator|+
literal|" retains initial key"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|initialCFKey
operator|.
name|getEncoded
argument_list|()
argument_list|,
name|extractHFileKey
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterKeyRotation
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create the table schema
name|TableDescriptorBuilder
name|tableDescriptorBuilder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"default"
argument_list|,
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
name|columnFamilyDescriptorBuilder
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
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
name|columnFamilyDescriptorBuilder
operator|.
name|setEncryptionType
argument_list|(
name|algorithm
argument_list|)
expr_stmt|;
name|columnFamilyDescriptorBuilder
operator|.
name|setEncryptionKey
argument_list|(
name|EncryptionUtil
operator|.
name|wrapKey
argument_list|(
name|conf
argument_list|,
literal|"hbase"
argument_list|,
name|initialCFKey
argument_list|)
argument_list|)
expr_stmt|;
name|tableDescriptorBuilder
operator|.
name|setColumnFamily
argument_list|(
name|columnFamilyDescriptorBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptor
name|tableDescriptor
init|=
name|tableDescriptorBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Create the table and some on disk files
name|createTableAndFlush
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
comment|// Verify we have store file(s) with the initial key
name|List
argument_list|<
name|Path
argument_list|>
name|storeFilePaths
init|=
name|findStorefilePaths
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|storeFilePaths
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
name|storeFilePaths
control|)
block|{
name|assertTrue
argument_list|(
literal|"Store file "
operator|+
name|path
operator|+
literal|" has incorrect key"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|initialCFKey
operator|.
name|getEncoded
argument_list|()
argument_list|,
name|extractHFileKey
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Now shut down the HBase cluster
name|TEST_UTIL
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
comment|// "Rotate" the master key
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_NAME_CONF_KEY
argument_list|,
literal|"other"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|CRYPTO_MASTERKEY_ALTERNATE_NAME_CONF_KEY
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
comment|// Start the cluster back up
name|TEST_UTIL
operator|.
name|startMiniHBaseCluster
argument_list|()
expr_stmt|;
comment|// Verify the table can still be loaded
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
comment|// Double check that the store file keys can be unwrapped
name|storeFilePaths
operator|=
name|findStorefilePaths
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|storeFilePaths
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
name|storeFilePaths
control|)
block|{
name|assertTrue
argument_list|(
literal|"Store file "
operator|+
name|path
operator|+
literal|" has incorrect key"
argument_list|,
name|Bytes
operator|.
name|equals
argument_list|(
name|initialCFKey
operator|.
name|getEncoded
argument_list|()
argument_list|,
name|extractHFileKey
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
for|for
control|(
name|HStore
name|store
range|:
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|getStores
argument_list|()
control|)
block|{
for|for
control|(
name|HStoreFile
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
name|List
argument_list|<
name|Path
argument_list|>
name|findCompactedStorefilePaths
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
name|getRegions
argument_list|(
name|tableName
argument_list|)
control|)
block|{
for|for
control|(
name|HStore
name|store
range|:
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|getStores
argument_list|()
control|)
block|{
name|Collection
argument_list|<
name|HStoreFile
argument_list|>
name|compactedfiles
init|=
name|store
operator|.
name|getStoreEngine
argument_list|()
operator|.
name|getStoreFileManager
argument_list|()
operator|.
name|getCompactedfiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|compactedfiles
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HStoreFile
name|storefile
range|:
name|compactedfiles
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
block|}
return|return
name|paths
return|;
block|}
specifier|private
name|void
name|createTableAndFlush
parameter_list|(
name|TableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|Exception
block|{
name|ColumnFamilyDescriptor
name|cfd
init|=
name|tableDescriptor
operator|.
name|getColumnFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
comment|// Create the test table
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableDescriptor
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
name|tableDescriptor
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
name|cfd
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
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
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
name|assertNotNull
argument_list|(
literal|"Crypto context has no key"
argument_list|,
name|key
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit


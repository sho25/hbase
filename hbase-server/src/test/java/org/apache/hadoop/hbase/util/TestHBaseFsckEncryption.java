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
name|util
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
name|regionserver
operator|.
name|HRegion
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
name|HStore
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
name|HStoreFile
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
name|Region
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
name|MiscTests
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
name|hbck
operator|.
name|HFileCorruptionChecker
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
name|hbck
operator|.
name|HbckTestingUtil
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
name|Before
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestHBaseFsckEncryption
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
name|TestHBaseFsckEncryption
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
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|TableDescriptor
name|tableDescriptor
decl_stmt|;
specifier|private
name|Key
name|cfKey
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
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
comment|// Create the test encryption key
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
name|cfKey
operator|=
operator|new
name|SecretKeySpec
argument_list|(
name|keyBytes
argument_list|,
name|algorithm
argument_list|)
expr_stmt|;
comment|// Start the minicluster
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// Create the table
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
literal|"TestHBaseFsckEncryption"
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|columnFamilyDescriptor
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
operator|.
name|setEncryptionType
argument_list|(
name|algorithm
argument_list|)
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
name|cfKey
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|tableDescriptorBuilder
operator|.
name|setColumnFamily
argument_list|(
name|columnFamilyDescriptor
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|=
name|tableDescriptorBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
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
block|}
annotation|@
name|After
specifier|public
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
name|testFsckWithEncryption
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Populate the table with some data
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
name|byte
index|[]
name|values
init|=
block|{
literal|'A'
block|,
literal|'B'
block|,
literal|'C'
block|,
literal|'D'
block|}
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
name|values
index|[
name|i
index|]
block|,
name|values
index|[
name|j
index|]
block|}
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{}
argument_list|,
operator|new
name|byte
index|[]
block|{
name|values
index|[
name|i
index|]
block|,
name|values
index|[
name|j
index|]
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Flush it
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
comment|// Verify we have encrypted store files on disk
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|paths
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
name|paths
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
name|paths
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
name|cfKey
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
comment|// Insure HBck doesn't consider them corrupt
name|HBaseFsck
name|res
init|=
name|HbckTestingUtil
operator|.
name|doHFileQuarantine
argument_list|(
name|conf
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|res
operator|.
name|getRetCode
argument_list|()
argument_list|)
expr_stmt|;
name|HFileCorruptionChecker
name|hfcc
init|=
name|res
operator|.
name|getHFilecorruptionChecker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getCorrupted
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getFailures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getQuarantined
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hfcc
operator|.
name|getMissing
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
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
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
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


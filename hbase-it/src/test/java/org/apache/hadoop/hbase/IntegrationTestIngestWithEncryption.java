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
name|Waiter
operator|.
name|Predicate
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
name|io
operator|.
name|hfile
operator|.
name|HFileReaderImpl
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
name|HFileWriterImpl
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
name|wal
operator|.
name|WAL
operator|.
name|Reader
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
name|wal
operator|.
name|WALProvider
operator|.
name|Writer
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
name|wal
operator|.
name|SecureProtobufLogReader
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
name|wal
operator|.
name|SecureProtobufLogWriter
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
name|IntegrationTests
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
name|EncryptionTest
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
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
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestIngestWithEncryption
extends|extends
name|IntegrationTestIngest
block|{
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IntegrationTestIngestWithEncryption
operator|.
name|class
argument_list|)
decl_stmt|;
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
static|static
block|{
comment|// These log level changes are only useful when running on a localhost
comment|// cluster.
name|Logger
operator|.
name|getLogger
argument_list|(
name|HFileReaderImpl
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|HFileWriterImpl
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|SecureProtobufLogReader
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
name|Logger
operator|.
name|getLogger
argument_list|(
name|SecureProtobufLogWriter
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
name|getTestingUtil
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|util
operator|.
name|isDistributedCluster
argument_list|()
condition|)
block|{
comment|// Inject required configuration if we are not running in distributed mode
name|conf
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
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
name|conf
operator|.
name|setClass
argument_list|(
literal|"hbase.regionserver.hlog.reader.impl"
argument_list|,
name|SecureProtobufLogReader
operator|.
name|class
argument_list|,
name|Reader
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
literal|"hbase.regionserver.hlog.writer.impl"
argument_list|,
name|SecureProtobufLogWriter
operator|.
name|class
argument_list|,
name|Writer
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_WAL_ENCRYPTION
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Check if the cluster configuration can support this test
try|try
block|{
name|EncryptionTest
operator|.
name|testEncryption
argument_list|(
name|conf
argument_list|,
literal|"AES"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Encryption configuration test did not pass, skipping test"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|super
operator|.
name|setUpCluster
argument_list|()
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Before
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Initialize the cluster. This invokes LoadTestTool -init_only, which
comment|// will create the test table, appropriately pre-split
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
return|return;
block|}
comment|// Update the test table schema so HFiles from this point will be written with
comment|// encryption features enabled.
specifier|final
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|TableDescriptor
name|tableDescriptor
init|=
name|admin
operator|.
name|getDescriptor
argument_list|(
name|getTablename
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ColumnFamilyDescriptor
name|columnDescriptor
range|:
name|tableDescriptor
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|ColumnFamilyDescriptor
name|updatedColumn
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|columnDescriptor
argument_list|)
operator|.
name|setEncryptionType
argument_list|(
literal|"AES"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Updating CF schema for "
operator|+
name|getTablename
argument_list|()
operator|+
literal|"."
operator|+
name|columnDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|getTablename
argument_list|()
argument_list|,
name|updatedColumn
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
literal|1000
argument_list|,
literal|true
argument_list|,
operator|new
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
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|getTablename
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|runTestFromCommandLine
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|super
operator|.
name|runTestFromCommandLine
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanUp
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
return|return;
block|}
name|super
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestIngestWithEncryption
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


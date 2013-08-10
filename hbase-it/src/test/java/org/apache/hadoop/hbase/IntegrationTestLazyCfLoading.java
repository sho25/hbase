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
name|security
operator|.
name|InvalidParameterException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|TreeMap
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
name|AtomicLong
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
name|filter
operator|.
name|CompareFilter
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|SingleColumnValueFilter
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|EnvironmentEdgeManager
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
name|MultiThreadedWriter
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
name|RegionSplitter
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
name|test
operator|.
name|LoadTestDataGenerator
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
name|test
operator|.
name|LoadTestKVGenerator
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
name|Assert
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

begin_comment
comment|/**  * Integration test that verifies lazy CF loading during scans by doing repeated scans  * with this feature while multiple threads are continuously writing values; and  * verifying the result.  */
end_comment

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
name|IntegrationTestLazyCfLoading
block|{
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|IntegrationTestLazyCfLoading
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TIMEOUT_KEY
init|=
literal|"hbase.%s.timeout"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ENCODING_KEY
init|=
literal|"hbase.%s.datablock.encoding"
decl_stmt|;
comment|/** A soft test timeout; duration of the test, as such, depends on number of keys to put. */
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_TIMEOUT_MINUTES
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_SERVERS
init|=
literal|1
decl_stmt|;
comment|/** Set regions per server low to ensure splits happen during test */
specifier|private
specifier|static
specifier|final
name|int
name|REGIONS_PER_SERVER
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|KEYS_TO_WRITE_PER_SERVER
init|=
literal|20000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|WRITER_THREADS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|WAIT_BETWEEN_SCANS_MS
init|=
literal|1000
decl_stmt|;
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
name|IntegrationTestLazyCfLoading
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|IntegrationTestingUtility
name|util
init|=
operator|new
name|IntegrationTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|DataGenerator
name|dataGen
init|=
operator|new
name|DataGenerator
argument_list|()
decl_stmt|;
comment|/** Custom LoadTestDataGenerator. Uses key generation and verification from    * LoadTestKVGenerator. Creates 3 column families; one with an integer column to    * filter on, the 2nd one with an integer column that matches the first integer column (for    * test-specific verification), and byte[] value that is used for general verification; and    * the third one with just the value.    */
specifier|private
specifier|static
class|class
name|DataGenerator
extends|extends
name|LoadTestDataGenerator
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MIN_DATA_SIZE
init|=
literal|4096
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_DATA_SIZE
init|=
literal|65536
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ESSENTIAL_CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"essential"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|JOINED_CF1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"joined"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|JOINED_CF2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"joined2"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|FILTER_COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"filter"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|VALUE_COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|ACCEPTED_VALUE
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|columnMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|expectedNumberOfKeys
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|totalNumberOfKeys
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|DataGenerator
parameter_list|()
block|{
name|super
argument_list|(
name|MIN_DATA_SIZE
argument_list|,
name|MAX_DATA_SIZE
argument_list|)
expr_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|ESSENTIAL_CF
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FILTER_COLUMN
block|}
argument_list|)
expr_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|JOINED_CF1
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FILTER_COLUMN
block|,
name|VALUE_COLUMN
block|}
argument_list|)
expr_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|JOINED_CF2
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|VALUE_COLUMN
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getExpectedNumberOfKeys
parameter_list|()
block|{
return|return
name|expectedNumberOfKeys
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|long
name|getTotalNumberOfKeys
parameter_list|()
block|{
return|return
name|totalNumberOfKeys
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getDeterministicUniqueKey
parameter_list|(
name|long
name|keyBase
parameter_list|)
block|{
return|return
name|LoadTestKVGenerator
operator|.
name|md5PrefixedKey
argument_list|(
name|keyBase
argument_list|)
operator|.
name|getBytes
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|getColumnFamilies
parameter_list|()
block|{
return|return
name|columnMap
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
name|columnMap
operator|.
name|size
argument_list|()
index|]
index|[]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|generateColumnsForCf
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|)
block|{
return|return
name|columnMap
operator|.
name|get
argument_list|(
name|cf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|generateValue
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|column
parameter_list|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|column
argument_list|,
name|FILTER_COLUMN
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// Random deterministic way to make some values "on" and others "off" for filters.
name|long
name|value
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|rowKey
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
argument_list|,
literal|16
argument_list|)
operator|&
name|ACCEPTED_VALUE
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|cf
argument_list|,
name|ESSENTIAL_CF
argument_list|)
operator|==
literal|0
condition|)
block|{
name|totalNumberOfKeys
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|value
operator|==
name|ACCEPTED_VALUE
condition|)
block|{
name|expectedNumberOfKeys
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|column
argument_list|,
name|VALUE_COLUMN
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
name|kvGenerator
operator|.
name|generateRandomSizeValue
argument_list|(
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|)
return|;
block|}
name|String
name|error
init|=
literal|"Unknown column "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|column
argument_list|)
decl_stmt|;
assert|assert
literal|false
operator|:
name|error
assert|;
throw|throw
operator|new
name|InvalidParameterException
argument_list|(
name|error
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|verify
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|column
argument_list|,
name|FILTER_COLUMN
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// Relies on the filter from getScanFilter being used.
return|return
name|Bytes
operator|.
name|toLong
argument_list|(
name|value
argument_list|)
operator|==
name|ACCEPTED_VALUE
return|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|column
argument_list|,
name|VALUE_COLUMN
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
name|LoadTestKVGenerator
operator|.
name|verify
argument_list|(
name|value
argument_list|,
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|)
return|;
block|}
return|return
literal|false
return|;
comment|// some bogus value from read, we don't expect any such thing.
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|verify
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnSet
parameter_list|)
block|{
return|return
name|columnMap
operator|.
name|get
argument_list|(
name|cf
argument_list|)
operator|.
name|length
operator|==
name|columnSet
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|Filter
name|getScanFilter
parameter_list|()
block|{
name|SingleColumnValueFilter
name|scf
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|ESSENTIAL_CF
argument_list|,
name|FILTER_COLUMN
argument_list|,
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ACCEPTED_VALUE
argument_list|)
argument_list|)
decl_stmt|;
name|scf
operator|.
name|setFilterIfMissing
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|scf
return|;
block|}
block|}
empty_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing cluster with "
operator|+
name|NUM_SERVERS
operator|+
literal|" servers"
argument_list|)
expr_stmt|;
name|util
operator|.
name|initializeCluster
argument_list|(
name|NUM_SERVERS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done initializing cluster"
argument_list|)
expr_stmt|;
name|createTable
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|deleteTable
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table"
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
name|String
name|encodingKey
init|=
name|String
operator|.
name|format
argument_list|(
name|ENCODING_KEY
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|DataBlockEncoding
name|blockEncoding
init|=
name|DataBlockEncoding
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|encodingKey
argument_list|,
literal|"FAST_DIFF"
argument_list|)
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|cf
range|:
name|dataGen
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|cf
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setDataBlockEncoding
argument_list|(
name|blockEncoding
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
name|int
name|serverCount
init|=
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|splits
init|=
operator|new
name|RegionSplitter
operator|.
name|HexStringSplit
argument_list|()
operator|.
name|split
argument_list|(
name|serverCount
operator|*
name|REGIONS_PER_SERVER
argument_list|)
decl_stmt|;
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|splits
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created table"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|deleteTable
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting table"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableDisabled
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted table"
argument_list|)
expr_stmt|;
block|}
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
name|deleteTable
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring the cluster"
argument_list|)
expr_stmt|;
name|util
operator|.
name|restoreCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done restoring the cluster"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadersAndWriters
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|timeoutKey
init|=
name|String
operator|.
name|format
argument_list|(
name|TIMEOUT_KEY
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|maxRuntime
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|timeoutKey
argument_list|,
name|DEFAULT_TIMEOUT_MINUTES
argument_list|)
decl_stmt|;
name|long
name|serverCount
init|=
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
decl_stmt|;
name|long
name|keysToWrite
init|=
name|serverCount
operator|*
name|KEYS_TO_WRITE_PER_SERVER
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
comment|// Create multi-threaded writer and start it. We write multiple columns/CFs and verify
comment|// their integrity, therefore multi-put is necessary.
name|MultiThreadedWriter
name|writer
init|=
operator|new
name|MultiThreadedWriter
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|writer
operator|.
name|setMultiPut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting writer; the number of keys to write is "
operator|+
name|keysToWrite
argument_list|)
expr_stmt|;
name|writer
operator|.
name|start
argument_list|(
literal|1
argument_list|,
name|keysToWrite
argument_list|,
name|WRITER_THREADS
argument_list|)
expr_stmt|;
comment|// Now, do scans.
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|timeLimit
init|=
name|now
operator|+
operator|(
name|maxRuntime
operator|*
literal|60000
operator|)
decl_stmt|;
name|boolean
name|isWriterDone
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|now
operator|<
name|timeLimit
operator|&&
operator|!
name|isWriterDone
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting the scan; wrote approximately "
operator|+
name|dataGen
operator|.
name|getTotalNumberOfKeys
argument_list|()
operator|+
literal|" keys"
argument_list|)
expr_stmt|;
name|isWriterDone
operator|=
name|writer
operator|.
name|isDone
argument_list|()
expr_stmt|;
if|if
condition|(
name|isWriterDone
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanning full result, writer is done"
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|cf
range|:
name|dataGen
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|cf
argument_list|)
expr_stmt|;
block|}
name|scan
operator|.
name|setFilter
argument_list|(
name|dataGen
operator|.
name|getScanFilter
argument_list|()
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setLoadColumnFamiliesOnDemand
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// The number of keys we can expect from scan - lower bound (before scan).
comment|// Not a strict lower bound - writer knows nothing about filters, so we report
comment|// this from generator. Writer might have generated the value but not put it yet.
name|long
name|onesGennedBeforeScan
init|=
name|dataGen
operator|.
name|getExpectedNumberOfKeys
argument_list|()
decl_stmt|;
name|long
name|startTs
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|long
name|resultCount
init|=
literal|0
decl_stmt|;
name|Result
name|result
init|=
literal|null
decl_stmt|;
comment|// Verify and count the results.
while|while
condition|(
operator|(
name|result
operator|=
name|results
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|boolean
name|isOk
init|=
name|writer
operator|.
name|verifyResultAgainstDataGenerator
argument_list|(
name|result
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Failed to verify ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"]"
argument_list|,
name|isOk
argument_list|)
expr_stmt|;
operator|++
name|resultCount
expr_stmt|;
block|}
name|long
name|timeTaken
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTs
decl_stmt|;
comment|// Verify the result count.
name|long
name|onesGennedAfterScan
init|=
name|dataGen
operator|.
name|getExpectedNumberOfKeys
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Read "
operator|+
name|resultCount
operator|+
literal|" keys when at most "
operator|+
name|onesGennedAfterScan
operator|+
literal|" were generated "
argument_list|,
name|onesGennedAfterScan
operator|>=
name|resultCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|isWriterDone
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Read "
operator|+
name|resultCount
operator|+
literal|" keys; the writer is done and "
operator|+
name|onesGennedAfterScan
operator|+
literal|" keys were generated"
argument_list|,
name|onesGennedAfterScan
operator|==
name|resultCount
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|onesGennedBeforeScan
operator|*
literal|0.9
operator|>
name|resultCount
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Read way too few keys ("
operator|+
name|resultCount
operator|+
literal|"/"
operator|+
name|onesGennedBeforeScan
operator|+
literal|") - there might be a problem, or the writer might just be slow"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Scan took "
operator|+
name|timeTaken
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isWriterDone
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|WAIT_BETWEEN_SCANS_MS
argument_list|)
expr_stmt|;
name|now
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"There are write failures"
argument_list|,
literal|0
argument_list|,
name|writer
operator|.
name|getNumWriteFailures
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Writer is not done"
argument_list|,
name|isWriterDone
argument_list|)
expr_stmt|;
comment|// Assert.fail("Boom!");
block|}
block|}
end_class

end_unit


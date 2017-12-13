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
name|client
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
name|assertTrue
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
name|KeepDeletedCells
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
name|exceptions
operator|.
name|DeserializationException
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
name|exceptions
operator|.
name|HBaseException
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
name|compress
operator|.
name|Compression
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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|regionserver
operator|.
name|BloomType
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
name|testclassification
operator|.
name|SmallTests
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
name|BuilderStyleTest
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
name|PrettyPrinter
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
name|ExpectedException
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestColumnFamilyDescriptorBuilder
block|{
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|expectedEx
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testBuilder
parameter_list|()
throws|throws
name|DeserializationException
block|{
name|ColumnFamilyDescriptorBuilder
name|builder
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|setInMemory
argument_list|(
literal|true
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|NONE
argument_list|)
decl_stmt|;
specifier|final
name|int
name|v
init|=
literal|123
decl_stmt|;
name|builder
operator|.
name|setBlocksize
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBlockCacheEnabled
argument_list|(
operator|!
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMaxVersions
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMinVersions
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getMinVersions
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setKeepDeletedCells
argument_list|(
name|KeepDeletedCells
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setInMemory
argument_list|(
operator|!
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|)
expr_stmt|;
name|boolean
name|inmemory
init|=
name|builder
operator|.
name|build
argument_list|()
operator|.
name|isInMemory
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setScope
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setDataBlockEncoding
argument_list|(
name|DataBlockEncoding
operator|.
name|FAST_DIFF
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBloomFilterType
argument_list|(
name|BloomType
operator|.
name|ROW
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCompressionType
argument_list|(
name|Algorithm
operator|.
name|SNAPPY
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMobThreshold
argument_list|(
literal|1000L
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setDFSReplication
argument_list|(
operator|(
name|short
operator|)
name|v
argument_list|)
expr_stmt|;
name|ColumnFamilyDescriptor
name|hcd
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|toByteArray
argument_list|(
name|hcd
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|deserializedHcd
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hcd
operator|.
name|equals
argument_list|(
name|deserializedHcd
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|hcd
operator|.
name|getBlocksize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|hcd
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hcd
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|,
name|deserializedHcd
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcd
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|deserializedHcd
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcd
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|deserializedHcd
operator|.
name|getMinVersions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcd
operator|.
name|getKeepDeletedCells
argument_list|()
argument_list|,
name|deserializedHcd
operator|.
name|getKeepDeletedCells
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|inmemory
argument_list|,
name|deserializedHcd
operator|.
name|isInMemory
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcd
operator|.
name|getScope
argument_list|()
argument_list|,
name|deserializedHcd
operator|.
name|getScope
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|deserializedHcd
operator|.
name|getCompressionType
argument_list|()
operator|.
name|equals
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|SNAPPY
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|deserializedHcd
operator|.
name|getDataBlockEncoding
argument_list|()
operator|.
name|equals
argument_list|(
name|DataBlockEncoding
operator|.
name|FAST_DIFF
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|deserializedHcd
operator|.
name|getBloomFilterType
argument_list|()
operator|.
name|equals
argument_list|(
name|BloomType
operator|.
name|ROW
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcd
operator|.
name|isMobEnabled
argument_list|()
argument_list|,
name|deserializedHcd
operator|.
name|isMobEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcd
operator|.
name|getMobThreshold
argument_list|()
argument_list|,
name|deserializedHcd
operator|.
name|getMobThreshold
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|deserializedHcd
operator|.
name|getDFSReplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests HColumnDescriptor with empty familyName    */
annotation|@
name|Test
specifier|public
name|void
name|testHColumnDescriptorShouldThrowIAEWhenFamilyNameEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|expectedEx
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedEx
operator|.
name|expectMessage
argument_list|(
literal|"Column Family name can not be empty"
argument_list|)
expr_stmt|;
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that we add and remove strings from configuration properly.    */
annotation|@
name|Test
specifier|public
name|void
name|testAddGetRemoveConfiguration
parameter_list|()
block|{
name|ColumnFamilyDescriptorBuilder
name|builder
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|key
init|=
literal|"Some"
decl_stmt|;
name|String
name|value
init|=
literal|"value"
decl_stmt|;
name|builder
operator|.
name|setConfiguration
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|value
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getConfigurationValue
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|removeConfiguration
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getConfigurationValue
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobValuesInHColumnDescriptorShouldReadable
parameter_list|()
block|{
name|boolean
name|isMob
init|=
literal|true
decl_stmt|;
name|long
name|threshold
init|=
literal|1000
decl_stmt|;
name|String
name|policy
init|=
literal|"weekly"
decl_stmt|;
comment|// We unify the format of all values saved in the descriptor.
comment|// Each value is stored as bytes of string.
name|String
name|isMobString
init|=
name|PrettyPrinter
operator|.
name|format
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|isMob
argument_list|)
argument_list|,
name|HColumnDescriptor
operator|.
name|getUnit
argument_list|(
name|HColumnDescriptor
operator|.
name|IS_MOB
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|thresholdString
init|=
name|PrettyPrinter
operator|.
name|format
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|threshold
argument_list|)
argument_list|,
name|HColumnDescriptor
operator|.
name|getUnit
argument_list|(
name|HColumnDescriptor
operator|.
name|MOB_THRESHOLD
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|policyString
init|=
name|PrettyPrinter
operator|.
name|format
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|policy
argument_list|)
argument_list|)
argument_list|,
name|HColumnDescriptor
operator|.
name|getUnit
argument_list|(
name|HColumnDescriptor
operator|.
name|MOB_COMPACT_PARTITION_POLICY
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|isMob
argument_list|)
argument_list|,
name|isMobString
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|threshold
argument_list|)
argument_list|,
name|thresholdString
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|policy
argument_list|)
argument_list|,
name|policyString
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClassMethodsAreBuilderStyle
parameter_list|()
block|{
comment|/* HColumnDescriptor should have a builder style setup where setXXX/addXXX methods      * can be chainable together:      * . For example:      * HColumnDescriptor hcd      *   = new HColumnDescriptor()      *     .setFoo(foo)      *     .setBar(bar)      *     .setBuz(buz)      *      * This test ensures that all methods starting with "set" returns the declaring object      */
name|BuilderStyleTest
operator|.
name|assertClassesAreBuilderStyle
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetTimeToLive
parameter_list|()
throws|throws
name|HBaseException
block|{
name|String
name|ttl
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
name|builder
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|ttl
operator|=
literal|"50000"
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|50000
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|ttl
operator|=
literal|"50000 seconds"
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|50000
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|ttl
operator|=
literal|""
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|ttl
operator|=
literal|"FOREVER"
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|FOREVER
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|ttl
operator|=
literal|"1 HOUR 10 minutes 1 second"
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4201
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|ttl
operator|=
literal|"500 Days 23 HOURS"
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|43282800
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
name|ttl
operator|=
literal|"43282800 SECONDS (500 Days 23 hours)"
expr_stmt|;
name|builder
operator|.
name|setTimeToLive
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|43282800
argument_list|,
name|builder
operator|.
name|build
argument_list|()
operator|.
name|getTimeToLive
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


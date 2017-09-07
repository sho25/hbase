begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|List
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
name|NavigableSet
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
name|Get
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
name|filter
operator|.
name|BinaryComparator
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
operator|.
name|CompareOp
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
name|PrefixFilter
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
name|RowFilter
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
name|TimeRange
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
operator|.
name|SplitLogTask
operator|.
name|RecoveryMode
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
name|io
operator|.
name|DataInputBuffer
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
comment|/**  * Test HBase Writables serializations  */
end_comment

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
name|TestSerialization
block|{
annotation|@
name|Test
specifier|public
name|void
name|testKeyValue
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|name
init|=
literal|"testKeyValue2"
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|name
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|fam
init|=
literal|"fam"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qf
init|=
literal|"qf"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"val"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qf
argument_list|,
name|ts
argument_list|,
name|val
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|long
name|l
init|=
name|KeyValueUtil
operator|.
name|write
argument_list|(
name|kv
argument_list|,
name|dos
argument_list|)
decl_stmt|;
name|dos
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|mb
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|mb
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|KeyValue
name|deserializedKv
init|=
name|KeyValueUtil
operator|.
name|create
argument_list|(
name|dis
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|deserializedKv
operator|.
name|getBuffer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|kv
operator|.
name|getOffset
argument_list|()
argument_list|,
name|deserializedKv
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|kv
operator|.
name|getLength
argument_list|()
argument_list|,
name|deserializedKv
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateKeyValueInvalidNegativeLength
parameter_list|()
block|{
name|KeyValue
name|kv_0
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myRow"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myCF"
argument_list|)
argument_list|,
comment|// 51 bytes
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myQualifier"
argument_list|)
argument_list|,
literal|12345L
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"my12345"
argument_list|)
argument_list|)
decl_stmt|;
name|KeyValue
name|kv_1
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myRow"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myCF"
argument_list|)
argument_list|,
comment|// 49 bytes
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myQualifier"
argument_list|)
argument_list|,
literal|12345L
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"my123"
argument_list|)
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|long
name|l
init|=
literal|0
decl_stmt|;
try|try
block|{
name|l
operator|=
name|KeyValue
operator|.
name|oswrite
argument_list|(
name|kv_0
argument_list|,
name|dos
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|l
operator|+=
name|KeyValue
operator|.
name|oswrite
argument_list|(
name|kv_1
argument_list|,
name|dos
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100L
argument_list|,
name|l
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected IOException"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
try|try
block|{
name|KeyValueUtil
operator|.
name|create
argument_list|(
name|dis
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|kv_0
operator|.
name|equals
argument_list|(
name|kv_1
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected Exception"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// length -1
try|try
block|{
comment|// even if we have a good kv now in dis we will just pass length with -1 for simplicity
name|KeyValueUtil
operator|.
name|create
argument_list|(
operator|-
literal|1
argument_list|,
name|dis
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected corrupt stream"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Failed read -1 bytes, stream corrupt?"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitLogTask
parameter_list|()
throws|throws
name|DeserializationException
block|{
name|SplitLogTask
name|slt
init|=
operator|new
name|SplitLogTask
operator|.
name|Unassigned
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"mgr,1,1"
argument_list|)
argument_list|,
name|RecoveryMode
operator|.
name|LOG_REPLAY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|slt
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|SplitLogTask
name|sltDeserialized
init|=
name|SplitLogTask
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|slt
operator|.
name|equals
argument_list|(
name|sltDeserialized
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|f
init|=
operator|new
name|RowFilter
argument_list|(
name|CompareOperator
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRowOne-2"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|f
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|Filter
name|ff
init|=
name|RowFilter
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|ff
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableDescriptor
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|name
init|=
literal|"testTableDescriptor"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|byte
index|[]
name|mb
init|=
name|htd
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|deserializedHtd
init|=
name|HTableDescriptor
operator|.
name|parseFrom
argument_list|(
name|mb
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|deserializedHtd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test RegionInfo serialization    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRegionInfo
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionInfo
name|hri
init|=
name|createRandomRegion
argument_list|(
literal|"testRegionInfo"
argument_list|)
decl_stmt|;
comment|//test toByteArray()
name|byte
index|[]
name|hrib
init|=
name|hri
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|HRegionInfo
name|deserializedHri
init|=
name|HRegionInfo
operator|.
name|parseFrom
argument_list|(
name|hrib
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|deserializedHri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri
argument_list|,
name|deserializedHri
argument_list|)
expr_stmt|;
comment|//test toDelimitedByteArray()
name|hrib
operator|=
name|hri
operator|.
name|toDelimitedByteArray
argument_list|()
expr_stmt|;
name|DataInputBuffer
name|buf
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
try|try
block|{
name|buf
operator|.
name|reset
argument_list|(
name|hrib
argument_list|,
name|hrib
operator|.
name|length
argument_list|)
expr_stmt|;
name|deserializedHri
operator|=
name|HRegionInfo
operator|.
name|parseFrom
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|deserializedHri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hri
argument_list|,
name|deserializedHri
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|buf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionInfos
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionInfo
name|hri
init|=
name|createRandomRegion
argument_list|(
literal|"testRegionInfos"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|triple
init|=
name|HRegionInfo
operator|.
name|toDelimitedByteArray
argument_list|(
name|hri
argument_list|,
name|hri
argument_list|,
name|hri
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|HRegionInfo
operator|.
name|parseDelimitedFrom
argument_list|(
name|triple
argument_list|,
literal|0
argument_list|,
name|triple
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|regions
operator|.
name|size
argument_list|()
operator|==
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|regions
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegionInfo
name|createRandomRegion
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|families
init|=
operator|new
name|String
index|[]
block|{
literal|"info"
block|,
literal|"anchor"
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
name|families
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|families
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
return|;
block|}
comment|/*    * TODO   @Test public void testPut() throws Exception{     byte[] row = "row".getBytes();     byte[] fam = "fam".getBytes();     byte[] qf1 = "qf1".getBytes();     byte[] qf2 = "qf2".getBytes();     byte[] qf3 = "qf3".getBytes();     byte[] qf4 = "qf4".getBytes();     byte[] qf5 = "qf5".getBytes();     byte[] qf6 = "qf6".getBytes();     byte[] qf7 = "qf7".getBytes();     byte[] qf8 = "qf8".getBytes();      long ts = System.currentTimeMillis();     byte[] val = "val".getBytes();      Put put = new Put(row);     put.setWriteToWAL(false);     put.add(fam, qf1, ts, val);     put.add(fam, qf2, ts, val);     put.add(fam, qf3, ts, val);     put.add(fam, qf4, ts, val);     put.add(fam, qf5, ts, val);     put.add(fam, qf6, ts, val);     put.add(fam, qf7, ts, val);     put.add(fam, qf8, ts, val);      byte[] sb = Writables.getBytes(put);     Put desPut = (Put)Writables.getWritable(sb, new Put());      //Timing test //    long start = System.nanoTime(); //    desPut = (Put)Writables.getWritable(sb, new Put()); //    long stop = System.nanoTime(); //    System.out.println("timer " +(stop-start));      assertTrue(Bytes.equals(put.getRow(), desPut.getRow()));     List<KeyValue> list = null;     List<KeyValue> desList = null;     for(Map.Entry<byte[], List<KeyValue>> entry : put.getFamilyMap().entrySet()){       assertTrue(desPut.getFamilyMap().containsKey(entry.getKey()));       list = entry.getValue();       desList = desPut.getFamilyMap().get(entry.getKey());       for(int i=0; i<list.size(); i++){         assertTrue(list.get(i).equals(desList.get(i)));       }     }   }     @Test public void testPut2() throws Exception{     byte[] row = "testAbort,,1243116656250".getBytes();     byte[] fam = "historian".getBytes();     byte[] qf1 = "creation".getBytes();      long ts = 9223372036854775807L;     byte[] val = "dont-care".getBytes();      Put put = new Put(row);     put.add(fam, qf1, ts, val);      byte[] sb = Writables.getBytes(put);     Put desPut = (Put)Writables.getWritable(sb, new Put());      assertTrue(Bytes.equals(put.getRow(), desPut.getRow()));     List<KeyValue> list = null;     List<KeyValue> desList = null;     for(Map.Entry<byte[], List<KeyValue>> entry : put.getFamilyMap().entrySet()){       assertTrue(desPut.getFamilyMap().containsKey(entry.getKey()));       list = entry.getValue();       desList = desPut.getFamilyMap().get(entry.getKey());       for(int i=0; i<list.size(); i++){         assertTrue(list.get(i).equals(desList.get(i)));       }     }   }     @Test public void testDelete() throws Exception{     byte[] row = "row".getBytes();     byte[] fam = "fam".getBytes();     byte[] qf1 = "qf1".getBytes();      long ts = System.currentTimeMillis();      Delete delete = new Delete(row);     delete.deleteColumn(fam, qf1, ts);      byte[] sb = Writables.getBytes(delete);     Delete desDelete = (Delete)Writables.getWritable(sb, new Delete());      assertTrue(Bytes.equals(delete.getRow(), desDelete.getRow()));     List<KeyValue> list = null;     List<KeyValue> desList = null;     for(Map.Entry<byte[], List<KeyValue>> entry :         delete.getFamilyMap().entrySet()){       assertTrue(desDelete.getFamilyMap().containsKey(entry.getKey()));       list = entry.getValue();       desList = desDelete.getFamilyMap().get(entry.getKey());       for(int i=0; i<list.size(); i++){         assertTrue(list.get(i).equals(desList.get(i)));       }     }   }   */
annotation|@
name|Test
specifier|public
name|void
name|testGet
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|row
init|=
literal|"row"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|fam
init|=
literal|"fam"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qf1
init|=
literal|"qf1"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|maxVersions
init|=
literal|2
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qf1
argument_list|)
expr_stmt|;
name|get
operator|.
name|setTimeRange
argument_list|(
name|ts
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|get
operator|.
name|setMaxVersions
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
name|ClientProtos
operator|.
name|Get
name|getProto
init|=
name|ProtobufUtil
operator|.
name|toGet
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Get
name|desGet
init|=
name|ProtobufUtil
operator|.
name|toGet
argument_list|(
name|getProto
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|,
name|desGet
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|desSet
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|get
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|desGet
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|set
operator|=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|desSet
operator|=
name|desGet
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|qualifier
range|:
name|set
control|)
block|{
name|assertTrue
argument_list|(
name|desSet
operator|.
name|contains
argument_list|(
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|get
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|desGet
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|tr
init|=
name|get
operator|.
name|getTimeRange
argument_list|()
decl_stmt|;
name|TimeRange
name|desTr
init|=
name|desGet
operator|.
name|getTimeRange
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|tr
operator|.
name|getMax
argument_list|()
argument_list|,
name|desTr
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tr
operator|.
name|getMin
argument_list|()
argument_list|,
name|desTr
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScan
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|startRow
init|=
literal|"startRow"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|stopRow
init|=
literal|"stopRow"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|fam
init|=
literal|"fam"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qf1
init|=
literal|"qf1"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|maxVersions
init|=
literal|2
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|startRow
argument_list|,
name|stopRow
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qf1
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setTimeRange
argument_list|(
name|ts
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|maxVersions
argument_list|)
expr_stmt|;
name|ClientProtos
operator|.
name|Scan
name|scanProto
init|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Scan
name|desScan
init|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|scanProto
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|desScan
operator|.
name|getStartRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|scan
operator|.
name|getStopRow
argument_list|()
argument_list|,
name|desScan
operator|.
name|getStopRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|scan
operator|.
name|getCacheBlocks
argument_list|()
argument_list|,
name|desScan
operator|.
name|getCacheBlocks
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|desSet
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|desScan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|set
operator|=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|desSet
operator|=
name|desScan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|set
control|)
block|{
name|assertTrue
argument_list|(
name|desSet
operator|.
name|contains
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Test filters are serialized properly.
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
specifier|final
name|String
name|name
init|=
literal|"testScan"
decl_stmt|;
name|byte
index|[]
name|prefix
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|PrefixFilter
argument_list|(
name|prefix
argument_list|)
argument_list|)
expr_stmt|;
name|scanProto
operator|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|desScan
operator|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|scanProto
argument_list|)
expr_stmt|;
name|Filter
name|f
init|=
name|desScan
operator|.
name|getFilter
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|f
operator|instanceof
name|PrefixFilter
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|scan
operator|.
name|getMaxVersions
argument_list|()
argument_list|,
name|desScan
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|TimeRange
name|tr
init|=
name|scan
operator|.
name|getTimeRange
argument_list|()
decl_stmt|;
name|TimeRange
name|desTr
init|=
name|desScan
operator|.
name|getTimeRange
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|tr
operator|.
name|getMax
argument_list|()
argument_list|,
name|desTr
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tr
operator|.
name|getMin
argument_list|()
argument_list|,
name|desTr
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * TODO   @Test public void testResultEmpty() throws Exception {     List<KeyValue> keys = new ArrayList<KeyValue>();     Result r = Result.newResult(keys);     assertTrue(r.isEmpty());     byte [] rb = Writables.getBytes(r);     Result deserializedR = (Result)Writables.getWritable(rb, new Result());     assertTrue(deserializedR.isEmpty());   }     @Test public void testResult() throws Exception {     byte [] rowA = Bytes.toBytes("rowA");     byte [] famA = Bytes.toBytes("famA");     byte [] qfA = Bytes.toBytes("qfA");     byte [] valueA = Bytes.toBytes("valueA");      byte [] rowB = Bytes.toBytes("rowB");     byte [] famB = Bytes.toBytes("famB");     byte [] qfB = Bytes.toBytes("qfB");     byte [] valueB = Bytes.toBytes("valueB");      KeyValue kvA = new KeyValue(rowA, famA, qfA, valueA);     KeyValue kvB = new KeyValue(rowB, famB, qfB, valueB);      Result result = Result.newResult(new KeyValue[]{kvA, kvB});      byte [] rb = Writables.getBytes(result);     Result deResult = (Result)Writables.getWritable(rb, new Result());      assertTrue("results are not equivalent, first key mismatch",         result.raw()[0].equals(deResult.raw()[0]));      assertTrue("results are not equivalent, second key mismatch",         result.raw()[1].equals(deResult.raw()[1]));      // Test empty Result     Result r = new Result();     byte [] b = Writables.getBytes(r);     Result deserialized = (Result)Writables.getWritable(b, new Result());     assertEquals(r.size(), deserialized.size());   }    @Test public void testResultDynamicBuild() throws Exception {     byte [] rowA = Bytes.toBytes("rowA");     byte [] famA = Bytes.toBytes("famA");     byte [] qfA = Bytes.toBytes("qfA");     byte [] valueA = Bytes.toBytes("valueA");      byte [] rowB = Bytes.toBytes("rowB");     byte [] famB = Bytes.toBytes("famB");     byte [] qfB = Bytes.toBytes("qfB");     byte [] valueB = Bytes.toBytes("valueB");      KeyValue kvA = new KeyValue(rowA, famA, qfA, valueA);     KeyValue kvB = new KeyValue(rowB, famB, qfB, valueB);      Result result = Result.newResult(new KeyValue[]{kvA, kvB});      byte [] rb = Writables.getBytes(result);       // Call getRow() first     Result deResult = (Result)Writables.getWritable(rb, new Result());     byte [] row = deResult.getRow();     assertTrue(Bytes.equals(row, rowA));      // Call sorted() first     deResult = (Result)Writables.getWritable(rb, new Result());     assertTrue("results are not equivalent, first key mismatch",         result.raw()[0].equals(deResult.raw()[0]));     assertTrue("results are not equivalent, second key mismatch",         result.raw()[1].equals(deResult.raw()[1]));      // Call raw() first     deResult = (Result)Writables.getWritable(rb, new Result());     assertTrue("results are not equivalent, first key mismatch",         result.raw()[0].equals(deResult.raw()[0]));     assertTrue("results are not equivalent, second key mismatch",         result.raw()[1].equals(deResult.raw()[1]));     }    @Test public void testResultArray() throws Exception {     byte [] rowA = Bytes.toBytes("rowA");     byte [] famA = Bytes.toBytes("famA");     byte [] qfA = Bytes.toBytes("qfA");     byte [] valueA = Bytes.toBytes("valueA");      byte [] rowB = Bytes.toBytes("rowB");     byte [] famB = Bytes.toBytes("famB");     byte [] qfB = Bytes.toBytes("qfB");     byte [] valueB = Bytes.toBytes("valueB");      KeyValue kvA = new KeyValue(rowA, famA, qfA, valueA);     KeyValue kvB = new KeyValue(rowB, famB, qfB, valueB);       Result result1 = Result.newResult(new KeyValue[]{kvA, kvB});     Result result2 = Result.newResult(new KeyValue[]{kvB});     Result result3 = Result.newResult(new KeyValue[]{kvB});      Result [] results = new Result [] {result1, result2, result3};      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();     DataOutputStream out = new DataOutputStream(byteStream);     Result.writeArray(out, results);      byte [] rb = byteStream.toByteArray();      DataInputBuffer in = new DataInputBuffer();     in.reset(rb, 0, rb.length);      Result [] deResults = Result.readArray(in);      assertTrue(results.length == deResults.length);      for(int i=0;i<results.length;i++) {       KeyValue [] keysA = results[i].raw();       KeyValue [] keysB = deResults[i].raw();       assertTrue(keysA.length == keysB.length);       for(int j=0;j<keysA.length;j++) {         assertTrue("Expected equivalent keys but found:\n" +             "KeyA : " + keysA[j].toString() + "\n" +             "KeyB : " + keysB[j].toString() + "\n" +             keysA.length + " total keys, " + i + "th so far"             ,keysA[j].equals(keysB[j]));       }     }    }    @Test public void testResultArrayEmpty() throws Exception {     List<KeyValue> keys = new ArrayList<KeyValue>();     Result r = Result.newResult(keys);     Result [] results = new Result [] {r};      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();     DataOutputStream out = new DataOutputStream(byteStream);      Result.writeArray(out, results);      results = null;      byteStream = new ByteArrayOutputStream();     out = new DataOutputStream(byteStream);     Result.writeArray(out, results);      byte [] rb = byteStream.toByteArray();      DataInputBuffer in = new DataInputBuffer();     in.reset(rb, 0, rb.length);      Result [] deResults = Result.readArray(in);      assertTrue(deResults.length == 0);      results = new Result[0];      byteStream = new ByteArrayOutputStream();     out = new DataOutputStream(byteStream);     Result.writeArray(out, results);      rb = byteStream.toByteArray();      in = new DataInputBuffer();     in.reset(rb, 0, rb.length);      deResults = Result.readArray(in);      assertTrue(deResults.length == 0);    }   */
specifier|protected
specifier|static
specifier|final
name|int
name|MAXVERSIONS
init|=
literal|3
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfamily1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfamily2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"colfamily3"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|COLUMNS
init|=
block|{
name|fam1
block|,
name|fam2
block|,
name|fam3
block|}
decl_stmt|;
comment|/**    * Create a table of name<code>name</code> with {@link #COLUMNS} for    * families.    * @param name Name to give table.    * @return Column descriptor.    */
specifier|protected
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
name|createTableDescriptor
argument_list|(
name|name
argument_list|,
name|MAXVERSIONS
argument_list|)
return|;
block|}
comment|/**    * Create a table of name<code>name</code> with {@link #COLUMNS} for    * families.    * @param name Name to give table.    * @param versions How many versions to allow per column.    * @return Column descriptor.    */
specifier|protected
name|HTableDescriptor
name|createTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|versions
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam1
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam2
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam3
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|htd
return|;
block|}
block|}
end_class

end_unit


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
name|rest
operator|.
name|model
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
name|assertFalse
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
name|util
operator|.
name|Iterator
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
name|testclassification
operator|.
name|RestTests
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
name|RestTests
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
name|TestStorageClusterStatusModel
extends|extends
name|TestModelBase
argument_list|<
name|StorageClusterStatusModel
argument_list|>
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
name|TestStorageClusterStatusModel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|TestStorageClusterStatusModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|StorageClusterStatusModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
operator|+
literal|"<ClusterStatus averageLoad=\"1.0\" regions=\"2\" requests=\"0\">"
operator|+
literal|"<DeadNodes/><LiveNodes>"
operator|+
literal|"<Node heapSizeMB=\"128\" maxHeapSizeMB=\"1024\" name=\"test1\" requests=\"0\" startCode=\"1245219839331\">"
operator|+
literal|"<Region currentCompactedKVs=\"1\" memstoreSizeMB=\"0\" name=\"aGJhc2U6cm9vdCwsMA==\" readRequestsCount=\"1\" "
operator|+
literal|"rootIndexSizeKB=\"1\" storefileIndexSizeKB=\"0\" storefileSizeMB=\"0\" storefiles=\"1\" stores=\"1\" "
operator|+
literal|"totalCompactingKVs=\"1\" totalStaticBloomSizeKB=\"1\" totalStaticIndexSizeKB=\"1\" writeRequestsCount=\"2\"/>"
operator|+
literal|"</Node>"
operator|+
literal|"<Node heapSizeMB=\"512\" maxHeapSizeMB=\"1024\" name=\"test2\" requests=\"0\" startCode=\"1245239331198\">"
operator|+
literal|"<Region currentCompactedKVs=\"1\" memstoreSizeMB=\"0\" name=\"aGJhc2U6bWV0YSwsMTI0NjAwMDA0MzcyNA==\" "
operator|+
literal|"readRequestsCount=\"1\" rootIndexSizeKB=\"1\" storefileIndexSizeKB=\"0\" storefileSizeMB=\"0\" "
operator|+
literal|"storefiles=\"1\" stores=\"1\" totalCompactingKVs=\"1\" totalStaticBloomSizeKB=\"1\" "
operator|+
literal|"totalStaticIndexSizeKB=\"1\" writeRequestsCount=\"2\"/></Node></LiveNodes></ClusterStatus>"
expr_stmt|;
name|AS_PB
operator|=
literal|"Cj8KBXRlc3QxEOO6i+eeJBgAIIABKIAIMicKDWhiYXNlOnJvb3QsLDAQARgBIAAoADAAOAFAAkgB"
operator|+
literal|"UAFYAWABaAEKSwoFdGVzdDIQ/pKx8J4kGAAggAQogAgyMwoZaGJhc2U6bWV0YSwsMTI0NjAwMDA0"
operator|+
literal|"MzcyNBABGAEgACgAMAA4AUACSAFQAVgBYAFoARgCIAApAAAAAAAA8D8="
expr_stmt|;
comment|//Using jackson will break json backward compatibilty for this representation
comment|//but the original one was broken as it would only print one Node element
comment|//so the format itself was broken
name|AS_JSON
operator|=
literal|"{\"regions\":2,\"requests\":0,\"averageLoad\":1.0,\"LiveNodes\":[{\"name\":\"test1\","
operator|+
literal|"\"Region\":[{\"name\":\"aGJhc2U6cm9vdCwsMA==\",\"stores\":1,\"storefiles\":1,"
operator|+
literal|"\"storefileSizeMB\":0,\"memStoreSizeMB\":0,\"storefileIndexSizeKB\":0,"
operator|+
literal|"\"readRequestsCount\":1,\"writeRequestsCount\":2,\"rootIndexSizeKB\":1,"
operator|+
literal|"\"totalStaticIndexSizeKB\":1,\"totalStaticBloomSizeKB\":1,\"totalCompactingKVs\":1,"
operator|+
literal|"\"currentCompactedKVs\":1}],\"requests\":0,\"startCode\":1245219839331,"
operator|+
literal|"\"heapSizeMB\":128,\"maxHeapSizeMB\":1024},{\"name\":\"test2\","
operator|+
literal|"\"Region\":[{\"name\":\"aGJhc2U6bWV0YSwsMTI0NjAwMDA0MzcyNA==\",\"stores\":1,"
operator|+
literal|"\"storefiles\":1,\"storefileSizeMB\":0,\"memStoreSizeMB\":0,\"storefileIndexSizeKB\":0,"
operator|+
literal|"\"readRequestsCount\":1,\"writeRequestsCount\":2,\"rootIndexSizeKB\":1,"
operator|+
literal|"\"totalStaticIndexSizeKB\":1,\"totalStaticBloomSizeKB\":1,\"totalCompactingKVs\":1,"
operator|+
literal|"\"currentCompactedKVs\":1}],\"requests\":0,\"startCode\":1245239331198,"
operator|+
literal|"\"heapSizeMB\":512,\"maxHeapSizeMB\":1024}],\"DeadNodes\":[]}"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|StorageClusterStatusModel
name|buildTestModel
parameter_list|()
block|{
name|StorageClusterStatusModel
name|model
init|=
operator|new
name|StorageClusterStatusModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setRegions
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|model
operator|.
name|setRequests
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|model
operator|.
name|setAverageLoad
argument_list|(
literal|1.0
argument_list|)
expr_stmt|;
name|model
operator|.
name|addLiveNode
argument_list|(
literal|"test1"
argument_list|,
literal|1245219839331L
argument_list|,
literal|128
argument_list|,
literal|1024
argument_list|)
operator|.
name|addRegion
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hbase:root,,0"
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|model
operator|.
name|addLiveNode
argument_list|(
literal|"test2"
argument_list|,
literal|1245239331198L
argument_list|,
literal|512
argument_list|,
literal|1024
argument_list|)
operator|.
name|addRegion
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|+
literal|",,1246000043724"
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
return|return
name|model
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|checkModel
parameter_list|(
name|StorageClusterStatusModel
name|model
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|model
operator|.
name|getRegions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|model
operator|.
name|getRequests
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.0
argument_list|,
name|model
operator|.
name|getAverageLoad
argument_list|()
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|StorageClusterStatusModel
operator|.
name|Node
argument_list|>
name|nodes
init|=
name|model
operator|.
name|getLiveNodes
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|StorageClusterStatusModel
operator|.
name|Node
name|node
init|=
name|nodes
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"test1"
argument_list|,
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1245219839331L
argument_list|,
name|node
operator|.
name|getStartCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|128
argument_list|,
name|node
operator|.
name|getHeapSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1024
argument_list|,
name|node
operator|.
name|getMaxHeapSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|StorageClusterStatusModel
operator|.
name|Node
operator|.
name|Region
argument_list|>
name|regions
init|=
name|node
operator|.
name|getRegions
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|StorageClusterStatusModel
operator|.
name|Node
operator|.
name|Region
name|region
init|=
name|regions
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|region
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"hbase:root,,0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getStores
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getStorefiles
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getStorefileSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getMemStoreSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getStorefileIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|region
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getRootIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getTotalStaticIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getTotalStaticBloomSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getTotalCompactingKVs
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getCurrentCompactedKVs
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|regions
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|node
operator|=
name|nodes
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test2"
argument_list|,
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1245239331198L
argument_list|,
name|node
operator|.
name|getStartCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|512
argument_list|,
name|node
operator|.
name|getHeapSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1024
argument_list|,
name|node
operator|.
name|getMaxHeapSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|regions
operator|=
name|node
operator|.
name|getRegions
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|region
operator|=
name|regions
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|region
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
operator|+
literal|",,1246000043724"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getStores
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getStorefiles
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getStorefileSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getMemStoreSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getStorefileIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|region
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getRootIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getTotalStaticIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getTotalStaticBloomSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getTotalCompactingKVs
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|region
operator|.
name|getCurrentCompactedKVs
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|regions
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|nodes
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


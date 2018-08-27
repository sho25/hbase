begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software   * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
operator|.
name|bucket
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|BlockCacheKey
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
name|BlockPriority
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
name|BlockType
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
name|CacheableDeserializerIdManager
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
name|HFileBlock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|BucketCacheProtos
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|BucketProtoUtils
block|{
specifier|private
name|BucketProtoUtils
parameter_list|()
block|{    }
specifier|static
name|BucketCacheProtos
operator|.
name|BucketCacheEntry
name|toPB
parameter_list|(
name|BucketCache
name|cache
parameter_list|)
block|{
return|return
name|BucketCacheProtos
operator|.
name|BucketCacheEntry
operator|.
name|newBuilder
argument_list|()
operator|.
name|setCacheCapacity
argument_list|(
name|cache
operator|.
name|getMaxSize
argument_list|()
argument_list|)
operator|.
name|setIoClass
argument_list|(
name|cache
operator|.
name|ioEngine
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setMapClass
argument_list|(
name|cache
operator|.
name|backingMap
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|putAllDeserializers
argument_list|(
name|CacheableDeserializerIdManager
operator|.
name|save
argument_list|()
argument_list|)
operator|.
name|setBackingMap
argument_list|(
name|BucketProtoUtils
operator|.
name|toPB
argument_list|(
name|cache
operator|.
name|backingMap
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|BucketCacheProtos
operator|.
name|BackingMap
name|toPB
parameter_list|(
name|Map
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketCache
operator|.
name|BucketEntry
argument_list|>
name|backingMap
parameter_list|)
block|{
name|BucketCacheProtos
operator|.
name|BackingMap
operator|.
name|Builder
name|builder
init|=
name|BucketCacheProtos
operator|.
name|BackingMap
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketCache
operator|.
name|BucketEntry
argument_list|>
name|entry
range|:
name|backingMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|addEntry
argument_list|(
name|BucketCacheProtos
operator|.
name|BackingMapEntry
operator|.
name|newBuilder
argument_list|()
operator|.
name|setKey
argument_list|(
name|toPB
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setValue
argument_list|(
name|toPB
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|BucketCacheProtos
operator|.
name|BlockCacheKey
name|toPB
parameter_list|(
name|BlockCacheKey
name|key
parameter_list|)
block|{
return|return
name|BucketCacheProtos
operator|.
name|BlockCacheKey
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHfilename
argument_list|(
name|key
operator|.
name|getHfileName
argument_list|()
argument_list|)
operator|.
name|setOffset
argument_list|(
name|key
operator|.
name|getOffset
argument_list|()
argument_list|)
operator|.
name|setPrimaryReplicaBlock
argument_list|(
name|key
operator|.
name|isPrimary
argument_list|()
argument_list|)
operator|.
name|setBlockType
argument_list|(
name|toPB
argument_list|(
name|key
operator|.
name|getBlockType
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|BucketCacheProtos
operator|.
name|BlockType
name|toPB
parameter_list|(
name|BlockType
name|blockType
parameter_list|)
block|{
switch|switch
condition|(
name|blockType
condition|)
block|{
case|case
name|DATA
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|data
return|;
case|case
name|META
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|meta
return|;
case|case
name|TRAILER
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|trailer
return|;
case|case
name|INDEX_V1
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|index_v1
return|;
case|case
name|FILE_INFO
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|file_info
return|;
case|case
name|LEAF_INDEX
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|leaf_index
return|;
case|case
name|ROOT_INDEX
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|root_index
return|;
case|case
name|BLOOM_CHUNK
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|bloom_chunk
return|;
case|case
name|ENCODED_DATA
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|encoded_data
return|;
case|case
name|GENERAL_BLOOM_META
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|general_bloom_meta
return|;
case|case
name|INTERMEDIATE_INDEX
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|intermediate_index
return|;
case|case
name|DELETE_FAMILY_BLOOM_META
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockType
operator|.
name|delete_family_bloom_meta
return|;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unrecognized BlockType."
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|BucketCacheProtos
operator|.
name|BucketEntry
name|toPB
parameter_list|(
name|BucketCache
operator|.
name|BucketEntry
name|entry
parameter_list|)
block|{
return|return
name|BucketCacheProtos
operator|.
name|BucketEntry
operator|.
name|newBuilder
argument_list|()
operator|.
name|setOffset
argument_list|(
name|entry
operator|.
name|offset
argument_list|()
argument_list|)
operator|.
name|setLength
argument_list|(
name|entry
operator|.
name|getLength
argument_list|()
argument_list|)
operator|.
name|setDeserialiserIndex
argument_list|(
name|entry
operator|.
name|deserialiserIndex
argument_list|)
operator|.
name|setAccessCounter
argument_list|(
name|entry
operator|.
name|getAccessCounter
argument_list|()
argument_list|)
operator|.
name|setPriority
argument_list|(
name|toPB
argument_list|(
name|entry
operator|.
name|getPriority
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|BucketCacheProtos
operator|.
name|BlockPriority
name|toPB
parameter_list|(
name|BlockPriority
name|p
parameter_list|)
block|{
switch|switch
condition|(
name|p
condition|)
block|{
case|case
name|MULTI
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockPriority
operator|.
name|multi
return|;
case|case
name|MEMORY
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockPriority
operator|.
name|memory
return|;
case|case
name|SINGLE
case|:
return|return
name|BucketCacheProtos
operator|.
name|BlockPriority
operator|.
name|single
return|;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unrecognized BlockPriority."
argument_list|)
throw|;
block|}
block|}
specifier|static
name|ConcurrentHashMap
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketCache
operator|.
name|BucketEntry
argument_list|>
name|fromPB
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|deserializers
parameter_list|,
name|BucketCacheProtos
operator|.
name|BackingMap
name|backingMap
parameter_list|)
throws|throws
name|IOException
block|{
name|ConcurrentHashMap
argument_list|<
name|BlockCacheKey
argument_list|,
name|BucketCache
operator|.
name|BucketEntry
argument_list|>
name|result
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|BucketCacheProtos
operator|.
name|BackingMapEntry
name|entry
range|:
name|backingMap
operator|.
name|getEntryList
argument_list|()
control|)
block|{
name|BucketCacheProtos
operator|.
name|BlockCacheKey
name|protoKey
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|BlockCacheKey
name|key
init|=
operator|new
name|BlockCacheKey
argument_list|(
name|protoKey
operator|.
name|getHfilename
argument_list|()
argument_list|,
name|protoKey
operator|.
name|getOffset
argument_list|()
argument_list|,
name|protoKey
operator|.
name|getPrimaryReplicaBlock
argument_list|()
argument_list|,
name|fromPb
argument_list|(
name|protoKey
operator|.
name|getBlockType
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|BucketCacheProtos
operator|.
name|BucketEntry
name|protoValue
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|BucketCache
operator|.
name|BucketEntry
name|value
init|=
operator|new
name|BucketCache
operator|.
name|BucketEntry
argument_list|(
name|protoValue
operator|.
name|getOffset
argument_list|()
argument_list|,
name|protoValue
operator|.
name|getLength
argument_list|()
argument_list|,
name|protoValue
operator|.
name|getAccessCounter
argument_list|()
argument_list|,
name|protoValue
operator|.
name|getPriority
argument_list|()
operator|==
name|BucketCacheProtos
operator|.
name|BlockPriority
operator|.
name|memory
argument_list|)
decl_stmt|;
comment|// This is the deserializer that we stored
name|int
name|oldIndex
init|=
name|protoValue
operator|.
name|getDeserialiserIndex
argument_list|()
decl_stmt|;
name|String
name|deserializerClass
init|=
name|deserializers
operator|.
name|get
argument_list|(
name|oldIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|deserializerClass
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Found deserializer index without matching entry."
argument_list|)
throw|;
block|}
comment|// Convert it to the identifier for the deserializer that we have in this runtime
if|if
condition|(
name|deserializerClass
operator|.
name|equals
argument_list|(
name|HFileBlock
operator|.
name|BlockDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|int
name|actualIndex
init|=
name|HFileBlock
operator|.
name|BLOCK_DESERIALIZER
operator|.
name|getDeserialiserIdentifier
argument_list|()
decl_stmt|;
name|value
operator|.
name|deserialiserIndex
operator|=
operator|(
name|byte
operator|)
name|actualIndex
expr_stmt|;
block|}
else|else
block|{
comment|// We could make this more plugable, but right now HFileBlock is the only implementation
comment|// of Cacheable outside of tests, so this might not ever matter.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown deserializer class found: "
operator|+
name|deserializerClass
argument_list|)
throw|;
block|}
name|result
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|BlockType
name|fromPb
parameter_list|(
name|BucketCacheProtos
operator|.
name|BlockType
name|blockType
parameter_list|)
block|{
switch|switch
condition|(
name|blockType
condition|)
block|{
case|case
name|data
case|:
return|return
name|BlockType
operator|.
name|DATA
return|;
case|case
name|meta
case|:
return|return
name|BlockType
operator|.
name|META
return|;
case|case
name|trailer
case|:
return|return
name|BlockType
operator|.
name|TRAILER
return|;
case|case
name|index_v1
case|:
return|return
name|BlockType
operator|.
name|INDEX_V1
return|;
case|case
name|file_info
case|:
return|return
name|BlockType
operator|.
name|FILE_INFO
return|;
case|case
name|leaf_index
case|:
return|return
name|BlockType
operator|.
name|LEAF_INDEX
return|;
case|case
name|root_index
case|:
return|return
name|BlockType
operator|.
name|ROOT_INDEX
return|;
case|case
name|bloom_chunk
case|:
return|return
name|BlockType
operator|.
name|BLOOM_CHUNK
return|;
case|case
name|encoded_data
case|:
return|return
name|BlockType
operator|.
name|ENCODED_DATA
return|;
case|case
name|general_bloom_meta
case|:
return|return
name|BlockType
operator|.
name|GENERAL_BLOOM_META
return|;
case|case
name|intermediate_index
case|:
return|return
name|BlockType
operator|.
name|INTERMEDIATE_INDEX
return|;
case|case
name|delete_family_bloom_meta
case|:
return|return
name|BlockType
operator|.
name|DELETE_FAMILY_BLOOM_META
return|;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unrecognized BlockType."
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

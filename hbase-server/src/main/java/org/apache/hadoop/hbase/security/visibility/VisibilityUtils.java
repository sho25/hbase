begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|visibility
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TagType
operator|.
name|VISIBILITY_TAG_TYPE
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|ArrayBackedTag
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
name|Cell
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
name|PrivateCellUtil
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
name|Tag
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
name|TagType
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
name|io
operator|.
name|util
operator|.
name|StreamUtils
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
name|ipc
operator|.
name|RpcServer
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|MultiUserAuthorizations
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|UserAuthorizations
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabel
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsRequest
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
name|AccessDeniedException
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
name|security
operator|.
name|visibility
operator|.
name|expression
operator|.
name|ExpressionNode
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
name|visibility
operator|.
name|expression
operator|.
name|LeafExpressionNode
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
name|visibility
operator|.
name|expression
operator|.
name|NonLeafExpressionNode
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
name|visibility
operator|.
name|expression
operator|.
name|Operator
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
name|ByteRange
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
name|SimpleMutableByteRange
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
name|ReflectionUtils
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

begin_comment
comment|/**  * Utility method to support visibility  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VisibilityUtils
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
name|VisibilityUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|VISIBILITY_LABEL_GENERATOR_CLASS
init|=
literal|"hbase.regionserver.scan.visibility.label.generator.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SYSTEM_LABEL
init|=
literal|"system"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Tag
name|SORTED_ORDINAL_SERIALIZATION_FORMAT_TAG
init|=
operator|new
name|ArrayBackedTag
argument_list|(
name|TagType
operator|.
name|VISIBILITY_EXP_SERIALIZATION_FORMAT_TAG_TYPE
argument_list|,
name|VisibilityConstants
operator|.
name|SORTED_ORDINAL_SERIALIZATION_FORMAT_TAG_VAL
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMMA
init|=
literal|","
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ExpressionParser
name|EXP_PARSER
init|=
operator|new
name|ExpressionParser
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ExpressionExpander
name|EXP_EXPANDER
init|=
operator|new
name|ExpressionExpander
argument_list|()
decl_stmt|;
comment|/**    * Creates the labels data to be written to zookeeper.    * @param existingLabels    * @return Bytes form of labels and their ordinal details to be written to zookeeper.    */
specifier|public
specifier|static
name|byte
index|[]
name|getDataToWriteToZooKeeper
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|existingLabels
parameter_list|)
block|{
name|VisibilityLabelsRequest
operator|.
name|Builder
name|visReqBuilder
init|=
name|VisibilityLabelsRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|entry
range|:
name|existingLabels
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|VisibilityLabel
operator|.
name|Builder
name|visLabBuilder
init|=
name|VisibilityLabel
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|visLabBuilder
operator|.
name|setLabel
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|visLabBuilder
operator|.
name|setOrdinal
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|visReqBuilder
operator|.
name|addVisLabel
argument_list|(
name|visLabBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|visReqBuilder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Creates the user auth data to be written to zookeeper.    * @param userAuths    * @return Bytes form of user auths details to be written to zookeeper.    */
specifier|public
specifier|static
name|byte
index|[]
name|getUserAuthsDataToWriteToZooKeeper
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|userAuths
parameter_list|)
block|{
name|MultiUserAuthorizations
operator|.
name|Builder
name|builder
init|=
name|MultiUserAuthorizations
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|entry
range|:
name|userAuths
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|UserAuthorizations
operator|.
name|Builder
name|userAuthsBuilder
init|=
name|UserAuthorizations
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|userAuthsBuilder
operator|.
name|setUser
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|label
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|userAuthsBuilder
operator|.
name|addAuth
argument_list|(
name|label
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addUserAuths
argument_list|(
name|userAuthsBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Reads back from the zookeeper. The data read here is of the form written by    * writeToZooKeeper(Map&lt;byte[], Integer&gt; entries).    *     * @param data    * @return Labels and their ordinal details    * @throws DeserializationException    */
specifier|public
specifier|static
name|List
argument_list|<
name|VisibilityLabel
argument_list|>
name|readLabelsFromZKData
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|DeserializationException
block|{
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
try|try
block|{
name|VisibilityLabelsRequest
operator|.
name|Builder
name|builder
init|=
name|VisibilityLabelsRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|data
argument_list|,
name|pblen
argument_list|,
name|data
operator|.
name|length
operator|-
name|pblen
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|getVisLabelList
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Reads back User auth data written to zookeeper.    * @param data    * @return User auth details    * @throws DeserializationException    */
specifier|public
specifier|static
name|MultiUserAuthorizations
name|readUserAuthsFromZKData
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|DeserializationException
block|{
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|data
argument_list|)
condition|)
block|{
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
try|try
block|{
name|MultiUserAuthorizations
operator|.
name|Builder
name|builder
init|=
name|MultiUserAuthorizations
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ProtobufUtil
operator|.
name|mergeFrom
argument_list|(
name|builder
argument_list|,
name|data
argument_list|,
name|pblen
argument_list|,
name|data
operator|.
name|length
operator|-
name|pblen
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @param conf The configuration to use    * @return Stack of ScanLabelGenerator instances. ScanLabelGenerator classes can be specified in    *         Configuration as comma separated list using key    *         "hbase.regionserver.scan.visibility.label.generator.class"    * @throws IllegalArgumentException    *           when any of the specified ScanLabelGenerator class can not be loaded.    */
specifier|public
specifier|static
name|List
argument_list|<
name|ScanLabelGenerator
argument_list|>
name|getScanLabelGenerators
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// There can be n SLG specified as comma separated in conf
name|String
name|slgClassesCommaSeparated
init|=
name|conf
operator|.
name|get
argument_list|(
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|)
decl_stmt|;
comment|// We have only System level SLGs now. The order of execution will be same as the order in the
comment|// comma separated config value
name|List
argument_list|<
name|ScanLabelGenerator
argument_list|>
name|slgs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|slgClassesCommaSeparated
argument_list|)
condition|)
block|{
name|String
index|[]
name|slgClasses
init|=
name|slgClassesCommaSeparated
operator|.
name|split
argument_list|(
name|COMMA
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|slgClass
range|:
name|slgClasses
control|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|ScanLabelGenerator
argument_list|>
name|slgKlass
decl_stmt|;
try|try
block|{
name|slgKlass
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|ScanLabelGenerator
argument_list|>
operator|)
name|conf
operator|.
name|getClassByName
argument_list|(
name|slgClass
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|slgs
operator|.
name|add
argument_list|(
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|slgKlass
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unable to find "
operator|+
name|slgClass
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// If no SLG is specified in conf, by default we'll add two SLGs
comment|// 1. FeedUserAuthScanLabelGenerator
comment|// 2. DefinedSetFilterScanLabelGenerator
comment|// This stacking will achieve the following default behavior:
comment|// 1. If there is no Auths in the scan, we will obtain the global defined set for the user
comment|//    from the labels table.
comment|// 2. If there is Auths in the scan, we will examine the passed in Auths and filter out the
comment|//    labels that the user is not entitled to. Then use the resulting label set.
if|if
condition|(
name|slgs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|slgs
operator|.
name|add
argument_list|(
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|FeedUserAuthScanLabelGenerator
operator|.
name|class
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|slgs
operator|.
name|add
argument_list|(
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|DefinedSetFilterScanLabelGenerator
operator|.
name|class
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|slgs
return|;
block|}
comment|/**    * Extract the visibility tags of the given Cell into the given List    * @param cell - the cell    * @param tags - the array that will be populated if visibility tags are present    * @return The visibility tags serialization format    */
specifier|public
specifier|static
name|Byte
name|extractVisibilityTags
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
parameter_list|)
block|{
name|Byte
name|serializationFormat
init|=
literal|null
decl_stmt|;
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|tagsIterator
init|=
name|PrivateCellUtil
operator|.
name|tagsIterator
argument_list|(
name|cell
argument_list|)
decl_stmt|;
while|while
condition|(
name|tagsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tag
name|tag
init|=
name|tagsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|.
name|getType
argument_list|()
operator|==
name|TagType
operator|.
name|VISIBILITY_EXP_SERIALIZATION_FORMAT_TAG_TYPE
condition|)
block|{
name|serializationFormat
operator|=
name|Tag
operator|.
name|getValueAsByte
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tag
operator|.
name|getType
argument_list|()
operator|==
name|VISIBILITY_TAG_TYPE
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|serializationFormat
return|;
block|}
comment|/**    * Extracts and partitions the visibility tags and nonVisibility Tags    *    * @param cell - the cell for which we would extract and partition the    * visibility and non visibility tags    * @param visTags    *          - all the visibilty tags of type TagType.VISIBILITY_TAG_TYPE would    *          be added to this list    * @param nonVisTags - all the non visibility tags would be added to this list    * @return - the serailization format of the tag. Can be null if no tags are found or    * if there is no visibility tag found    */
specifier|public
specifier|static
name|Byte
name|extractAndPartitionTags
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|List
argument_list|<
name|Tag
argument_list|>
name|visTags
parameter_list|,
name|List
argument_list|<
name|Tag
argument_list|>
name|nonVisTags
parameter_list|)
block|{
name|Byte
name|serializationFormat
init|=
literal|null
decl_stmt|;
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|tagsIterator
init|=
name|PrivateCellUtil
operator|.
name|tagsIterator
argument_list|(
name|cell
argument_list|)
decl_stmt|;
while|while
condition|(
name|tagsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tag
name|tag
init|=
name|tagsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|.
name|getType
argument_list|()
operator|==
name|TagType
operator|.
name|VISIBILITY_EXP_SERIALIZATION_FORMAT_TAG_TYPE
condition|)
block|{
name|serializationFormat
operator|=
name|Tag
operator|.
name|getValueAsByte
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tag
operator|.
name|getType
argument_list|()
operator|==
name|VISIBILITY_TAG_TYPE
condition|)
block|{
name|visTags
operator|.
name|add
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// ignore string encoded visibility expressions, will be added in replication handling
name|nonVisTags
operator|.
name|add
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|serializationFormat
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isVisibilityTagsPresent
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|tagsIterator
init|=
name|PrivateCellUtil
operator|.
name|tagsIterator
argument_list|(
name|cell
argument_list|)
decl_stmt|;
while|while
condition|(
name|tagsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tag
name|tag
init|=
name|tagsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|.
name|getType
argument_list|()
operator|==
name|VISIBILITY_TAG_TYPE
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
specifier|static
name|Filter
name|createVisibilityLabelFilter
parameter_list|(
name|Region
name|region
parameter_list|,
name|Authorizations
name|authorizations
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|ByteRange
argument_list|,
name|Integer
argument_list|>
name|cfVsMaxVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnFamilyDescriptor
name|hcd
range|:
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|cfVsMaxVersions
operator|.
name|put
argument_list|(
operator|new
name|SimpleMutableByteRange
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|hcd
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|VisibilityLabelService
name|vls
init|=
name|VisibilityLabelServiceManager
operator|.
name|getInstance
argument_list|()
operator|.
name|getVisibilityLabelService
argument_list|()
decl_stmt|;
name|Filter
name|visibilityLabelFilter
init|=
operator|new
name|VisibilityLabelFilter
argument_list|(
name|vls
operator|.
name|getVisibilityExpEvaluator
argument_list|(
name|authorizations
argument_list|)
argument_list|,
name|cfVsMaxVersions
argument_list|)
decl_stmt|;
return|return
name|visibilityLabelFilter
return|;
block|}
comment|/**    * @return User who called RPC method. For non-RPC handling, falls back to system user    * @throws IOException When there is IOE in getting the system user (During non-RPC handling).    */
specifier|public
specifier|static
name|User
name|getActiveUser
parameter_list|()
throws|throws
name|IOException
block|{
name|Optional
argument_list|<
name|User
argument_list|>
name|optionalUser
init|=
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
decl_stmt|;
name|User
name|user
decl_stmt|;
if|if
condition|(
name|optionalUser
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|user
operator|=
name|optionalUser
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|user
operator|=
name|User
operator|.
name|getCurrent
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Current active user name is "
operator|+
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|user
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Tag
argument_list|>
name|createVisibilityExpTags
parameter_list|(
name|String
name|visExpression
parameter_list|,
name|boolean
name|withSerializationFormat
parameter_list|,
name|boolean
name|checkAuths
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|auths
parameter_list|,
name|VisibilityLabelOrdinalProvider
name|ordinalProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|ExpressionNode
name|node
init|=
literal|null
decl_stmt|;
try|try
block|{
name|node
operator|=
name|EXP_PARSER
operator|.
name|parse
argument_list|(
name|visExpression
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|node
operator|=
name|EXP_EXPANDER
operator|.
name|expand
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|List
argument_list|<
name|Integer
argument_list|>
name|labelOrdinals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// We will be adding this tag before the visibility tags and the presence of this
comment|// tag indicates we are supporting deletes with cell visibility
if|if
condition|(
name|withSerializationFormat
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
name|VisibilityUtils
operator|.
name|SORTED_ORDINAL_SERIALIZATION_FORMAT_TAG
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|node
operator|.
name|isSingleNode
argument_list|()
condition|)
block|{
name|getLabelOrdinals
argument_list|(
name|node
argument_list|,
name|labelOrdinals
argument_list|,
name|auths
argument_list|,
name|checkAuths
argument_list|,
name|ordinalProvider
argument_list|)
expr_stmt|;
name|writeLabelOrdinalsToStream
argument_list|(
name|labelOrdinals
argument_list|,
name|dos
argument_list|)
expr_stmt|;
name|tags
operator|.
name|add
argument_list|(
operator|new
name|ArrayBackedTag
argument_list|(
name|VISIBILITY_TAG_TYPE
argument_list|,
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|NonLeafExpressionNode
name|nlNode
init|=
operator|(
name|NonLeafExpressionNode
operator|)
name|node
decl_stmt|;
if|if
condition|(
name|nlNode
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|OR
condition|)
block|{
for|for
control|(
name|ExpressionNode
name|child
range|:
name|nlNode
operator|.
name|getChildExps
argument_list|()
control|)
block|{
name|getLabelOrdinals
argument_list|(
name|child
argument_list|,
name|labelOrdinals
argument_list|,
name|auths
argument_list|,
name|checkAuths
argument_list|,
name|ordinalProvider
argument_list|)
expr_stmt|;
name|writeLabelOrdinalsToStream
argument_list|(
name|labelOrdinals
argument_list|,
name|dos
argument_list|)
expr_stmt|;
name|tags
operator|.
name|add
argument_list|(
operator|new
name|ArrayBackedTag
argument_list|(
name|VISIBILITY_TAG_TYPE
argument_list|,
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|labelOrdinals
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|getLabelOrdinals
argument_list|(
name|nlNode
argument_list|,
name|labelOrdinals
argument_list|,
name|auths
argument_list|,
name|checkAuths
argument_list|,
name|ordinalProvider
argument_list|)
expr_stmt|;
name|writeLabelOrdinalsToStream
argument_list|(
name|labelOrdinals
argument_list|,
name|dos
argument_list|)
expr_stmt|;
name|tags
operator|.
name|add
argument_list|(
operator|new
name|ArrayBackedTag
argument_list|(
name|VISIBILITY_TAG_TYPE
argument_list|,
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|tags
return|;
block|}
specifier|private
specifier|static
name|void
name|getLabelOrdinals
parameter_list|(
name|ExpressionNode
name|node
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|labelOrdinals
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|auths
parameter_list|,
name|boolean
name|checkAuths
parameter_list|,
name|VisibilityLabelOrdinalProvider
name|ordinalProvider
parameter_list|)
throws|throws
name|IOException
throws|,
name|InvalidLabelException
block|{
if|if
condition|(
name|node
operator|.
name|isSingleNode
argument_list|()
condition|)
block|{
name|String
name|identifier
init|=
literal|null
decl_stmt|;
name|int
name|labelOrdinal
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|node
operator|instanceof
name|LeafExpressionNode
condition|)
block|{
name|identifier
operator|=
operator|(
operator|(
name|LeafExpressionNode
operator|)
name|node
operator|)
operator|.
name|getIdentifier
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"The identifier is "
operator|+
name|identifier
argument_list|)
expr_stmt|;
block|}
name|labelOrdinal
operator|=
name|ordinalProvider
operator|.
name|getLabelOrdinal
argument_list|(
name|identifier
argument_list|)
expr_stmt|;
name|checkAuths
argument_list|(
name|auths
argument_list|,
name|labelOrdinal
argument_list|,
name|identifier
argument_list|,
name|checkAuths
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// This is a NOT node.
name|LeafExpressionNode
name|lNode
init|=
call|(
name|LeafExpressionNode
call|)
argument_list|(
operator|(
name|NonLeafExpressionNode
operator|)
name|node
argument_list|)
operator|.
name|getChildExps
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|identifier
operator|=
name|lNode
operator|.
name|getIdentifier
argument_list|()
expr_stmt|;
name|labelOrdinal
operator|=
name|ordinalProvider
operator|.
name|getLabelOrdinal
argument_list|(
name|identifier
argument_list|)
expr_stmt|;
name|checkAuths
argument_list|(
name|auths
argument_list|,
name|labelOrdinal
argument_list|,
name|identifier
argument_list|,
name|checkAuths
argument_list|)
expr_stmt|;
name|labelOrdinal
operator|=
operator|-
literal|1
operator|*
name|labelOrdinal
expr_stmt|;
comment|// Store NOT node as -ve ordinal.
block|}
if|if
condition|(
name|labelOrdinal
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|InvalidLabelException
argument_list|(
literal|"Invalid visibility label "
operator|+
name|identifier
argument_list|)
throw|;
block|}
name|labelOrdinals
operator|.
name|add
argument_list|(
name|labelOrdinal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|ExpressionNode
argument_list|>
name|childExps
init|=
operator|(
operator|(
name|NonLeafExpressionNode
operator|)
name|node
operator|)
operator|.
name|getChildExps
argument_list|()
decl_stmt|;
for|for
control|(
name|ExpressionNode
name|child
range|:
name|childExps
control|)
block|{
name|getLabelOrdinals
argument_list|(
name|child
argument_list|,
name|labelOrdinals
argument_list|,
name|auths
argument_list|,
name|checkAuths
argument_list|,
name|ordinalProvider
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This will sort the passed labels in ascending oder and then will write one after the other to    * the passed stream.    * @param labelOrdinals    *          Unsorted label ordinals    * @param dos    *          Stream where to write the labels.    * @throws IOException    *           When IOE during writes to Stream.    */
specifier|private
specifier|static
name|void
name|writeLabelOrdinalsToStream
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|labelOrdinals
parameter_list|,
name|DataOutputStream
name|dos
parameter_list|)
throws|throws
name|IOException
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|labelOrdinals
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|labelOrdinal
range|:
name|labelOrdinals
control|)
block|{
name|StreamUtils
operator|.
name|writeRawVInt32
argument_list|(
name|dos
argument_list|,
name|labelOrdinal
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkAuths
parameter_list|(
name|Set
argument_list|<
name|Integer
argument_list|>
name|auths
parameter_list|,
name|int
name|labelOrdinal
parameter_list|,
name|String
name|identifier
parameter_list|,
name|boolean
name|checkAuths
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|checkAuths
condition|)
block|{
if|if
condition|(
name|auths
operator|==
literal|null
operator|||
operator|(
operator|!
name|auths
operator|.
name|contains
argument_list|(
name|labelOrdinal
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|AccessDeniedException
argument_list|(
literal|"Visibility label "
operator|+
name|identifier
operator|+
literal|" not authorized for the user "
operator|+
name|VisibilityUtils
operator|.
name|getActiveUser
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit


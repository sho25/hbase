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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|HBaseZeroCopyByteString
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
name|lang
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
name|hadoop
operator|.
name|classification
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
name|util
operator|.
name|ReflectionUtils
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
name|InvalidProtocolBufferException
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
name|byte
name|VISIBILITY_TAG_TYPE
init|=
name|TagType
operator|.
name|VISIBILITY_TAG_TYPE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SYSTEM_LABEL
init|=
literal|"system"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMMA
init|=
literal|","
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
name|HBaseZeroCopyByteString
operator|.
name|wrap
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
name|HBaseZeroCopyByteString
operator|.
name|wrap
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
comment|/**    * Reads back from the zookeeper. The data read here is of the form written by    * writeToZooKeeper(Map<byte[], Integer> entries).    *     * @param data    * @return Labels and their ordinal details    * @throws DeserializationException    */
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
name|request
init|=
name|VisibilityLabelsRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
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
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|request
operator|.
name|getVisLabelList
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
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
name|multiUserAuths
init|=
name|MultiUserAuthorizations
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
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
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|multiUserAuths
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
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
throws|throws
name|IOException
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
argument_list|<
name|ScanLabelGenerator
argument_list|>
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// If the conf is not configured by default we need to have one SLG to be used
comment|// ie. DefaultScanLabelGenerator
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
name|DefaultScanLabelGenerator
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
block|}
end_class

end_unit


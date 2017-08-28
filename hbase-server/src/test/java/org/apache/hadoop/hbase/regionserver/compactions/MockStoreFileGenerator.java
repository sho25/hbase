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
operator|.
name|compactions
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|MoreObjects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Random
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
name|RandomStringUtils
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
name|regionserver
operator|.
name|StoreFile
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
name|StoreFileReader
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Base class of objects that can create mock store files with a given size.  */
end_comment

begin_class
class|class
name|MockStoreFileGenerator
block|{
comment|/** How many chars long the store file name will be. */
specifier|private
specifier|static
specifier|final
name|int
name|FILENAME_LENGTH
init|=
literal|10
decl_stmt|;
comment|/** The random number generator. */
specifier|protected
name|Random
name|random
decl_stmt|;
name|MockStoreFileGenerator
parameter_list|(
name|Class
name|klass
parameter_list|)
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
name|klass
operator|.
name|getSimpleName
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|List
argument_list|<
name|StoreFile
argument_list|>
name|createStoreFileList
parameter_list|(
specifier|final
name|int
index|[]
name|fs
parameter_list|)
block|{
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFiles
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|fileSize
range|:
name|fs
control|)
block|{
name|storeFiles
operator|.
name|add
argument_list|(
name|createMockStoreFile
argument_list|(
name|fileSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|storeFiles
return|;
block|}
specifier|protected
name|StoreFile
name|createMockStoreFile
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
return|return
name|createMockStoreFile
argument_list|(
name|size
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
operator|-
literal|1L
argument_list|)
return|;
block|}
specifier|protected
name|StoreFile
name|createMockStoreFileBytes
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
return|return
name|createMockStoreFile
argument_list|(
name|size
argument_list|,
operator|-
literal|1L
argument_list|)
return|;
block|}
specifier|protected
name|StoreFile
name|createMockStoreFile
parameter_list|(
specifier|final
name|long
name|sizeInBytes
parameter_list|,
specifier|final
name|long
name|seqId
parameter_list|)
block|{
name|StoreFile
name|mockSf
init|=
name|mock
argument_list|(
name|StoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
name|StoreFileReader
name|reader
init|=
name|mock
argument_list|(
name|StoreFileReader
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|stringPath
init|=
literal|"/hbase/testTable/regionA/"
operator|+
name|RandomStringUtils
operator|.
name|random
argument_list|(
name|FILENAME_LENGTH
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
name|random
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|stringPath
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|reader
operator|.
name|getSequenceID
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|seqId
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|reader
operator|.
name|getTotalUncompressedBytes
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|reader
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockSf
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockSf
operator|.
name|excludeFromMinorCompaction
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockSf
operator|.
name|isReference
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// TODO come back to
comment|// this when selection takes this into account
name|when
argument_list|(
name|mockSf
operator|.
name|getReader
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|String
name|toString
init|=
name|MoreObjects
operator|.
name|toStringHelper
argument_list|(
literal|"MockStoreFile"
argument_list|)
operator|.
name|add
argument_list|(
literal|"isReference"
argument_list|,
literal|false
argument_list|)
operator|.
name|add
argument_list|(
literal|"fileSize"
argument_list|,
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|sizeInBytes
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
literal|"seqId"
argument_list|,
name|seqId
argument_list|)
operator|.
name|add
argument_list|(
literal|"path"
argument_list|,
name|stringPath
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|mockSf
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|toString
argument_list|)
expr_stmt|;
return|return
name|mockSf
return|;
block|}
block|}
end_class

end_unit


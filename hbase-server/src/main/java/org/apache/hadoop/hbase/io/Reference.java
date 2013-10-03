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
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|fs
operator|.
name|FSDataOutputStream
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
name|FileSystem
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
name|KeyValue
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
name|FSProtos
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_comment
comment|/**  * A reference to the top or bottom half of a store file where 'bottom' is the first half  * of the file containing the keys that sort lowest and 'top' is the second half  * of the file with keys that sort greater than those of the bottom half.  The file referenced  * lives under a different region.  References are made at region split time.  *  *<p>References work with a special half store file type.  References know how  * to write out the reference format in the file system and are what is juggled  * when references are mixed in with direct store files.  The half store file  * type is used reading the referred to file.  *  *<p>References to store files located over in some other region look like  * this in the file system  *<code>1278437856009925445.3323223323</code>:  * i.e. an id followed by hash of the referenced region.  * Note, a region is itself not splittable if it has instances of store file  * references.  References are cleaned up by compactions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Reference
block|{
specifier|private
name|byte
index|[]
name|splitkey
decl_stmt|;
specifier|private
name|Range
name|region
decl_stmt|;
comment|/**    * For split HStoreFiles, it specifies if the file covers the lower half or    * the upper half of the key range    */
specifier|static
enum|enum
name|Range
block|{
comment|/** HStoreFile contains upper half of key range */
name|top
block|,
comment|/** HStoreFile contains lower half of key range */
name|bottom
block|}
comment|/**    * @param splitRow    * @return A {@link Reference} that points at top half of a an hfile    */
specifier|public
specifier|static
name|Reference
name|createTopReference
parameter_list|(
specifier|final
name|byte
index|[]
name|splitRow
parameter_list|)
block|{
return|return
operator|new
name|Reference
argument_list|(
name|splitRow
argument_list|,
name|Range
operator|.
name|top
argument_list|)
return|;
block|}
comment|/**    * @param splitRow    * @return A {@link Reference} that points at the bottom half of a an hfile    */
specifier|public
specifier|static
name|Reference
name|createBottomReference
parameter_list|(
specifier|final
name|byte
index|[]
name|splitRow
parameter_list|)
block|{
return|return
operator|new
name|Reference
argument_list|(
name|splitRow
argument_list|,
name|Range
operator|.
name|bottom
argument_list|)
return|;
block|}
comment|/**    * Constructor    * @param splitRow This is row we are splitting around.    * @param fr    */
name|Reference
parameter_list|(
specifier|final
name|byte
index|[]
name|splitRow
parameter_list|,
specifier|final
name|Range
name|fr
parameter_list|)
block|{
name|this
operator|.
name|splitkey
operator|=
name|splitRow
operator|==
literal|null
condition|?
literal|null
else|:
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|splitRow
argument_list|)
operator|.
name|getKey
argument_list|()
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|fr
expr_stmt|;
block|}
comment|/**    * Used by serializations.    */
annotation|@
name|Deprecated
comment|// Make this private when it comes time to let go of this constructor.  Needed by pb serialization.
specifier|public
name|Reference
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|Range
operator|.
name|bottom
argument_list|)
expr_stmt|;
block|}
comment|/**    *    * @return Range    */
specifier|public
name|Range
name|getFileRegion
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
return|;
block|}
comment|/**    * @return splitKey    */
specifier|public
name|byte
index|[]
name|getSplitKey
parameter_list|()
block|{
return|return
name|splitkey
return|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
name|this
operator|.
name|region
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isTopFileRegion
parameter_list|(
specifier|final
name|Range
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|equals
argument_list|(
name|Range
operator|.
name|top
argument_list|)
return|;
block|}
comment|/**    * @deprecated Writables are going away. Use the pb serialization methods instead.    * Remove in a release after 0.96 goes out.  This is here only to migrate    * old Reference files written with Writables before 0.96.    */
annotation|@
name|Deprecated
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|tmp
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
comment|// If true, set region to top.
name|this
operator|.
name|region
operator|=
name|tmp
condition|?
name|Range
operator|.
name|top
else|:
name|Range
operator|.
name|bottom
expr_stmt|;
name|this
operator|.
name|splitkey
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Path
name|write
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|p
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|out
operator|.
name|write
argument_list|(
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
comment|/**    * Read a Reference from FileSystem.    * @param fs    * @param p    * @return New Reference made from passed<code>p</code>    * @throws IOException    */
specifier|public
specifier|static
name|Reference
name|read
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|p
argument_list|)
decl_stmt|;
try|try
block|{
comment|// I need to be able to move back in the stream if this is not a pb serialization so I can
comment|// do the Writable decoding instead.
name|in
operator|=
name|in
operator|.
name|markSupported
argument_list|()
condition|?
name|in
else|:
operator|new
name|BufferedInputStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|pblen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|in
operator|.
name|mark
argument_list|(
name|pblen
argument_list|)
expr_stmt|;
name|byte
index|[]
name|pbuf
init|=
operator|new
name|byte
index|[
name|pblen
index|]
decl_stmt|;
name|int
name|read
init|=
name|in
operator|.
name|read
argument_list|(
name|pbuf
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|!=
name|pblen
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"read="
operator|+
name|read
operator|+
literal|", wanted="
operator|+
name|pblen
argument_list|)
throw|;
comment|// WATCHOUT! Return in middle of function!!!
if|if
condition|(
name|ProtobufUtil
operator|.
name|isPBMagicPrefix
argument_list|(
name|pbuf
argument_list|)
condition|)
return|return
name|convert
argument_list|(
name|FSProtos
operator|.
name|Reference
operator|.
name|parseFrom
argument_list|(
name|in
argument_list|)
argument_list|)
return|;
comment|// Else presume Writables.  Need to reset the stream since it didn't start w/ pb.
comment|// We won't bother rewriting thie Reference as a pb since Reference is transitory.
name|in
operator|.
name|reset
argument_list|()
expr_stmt|;
name|Reference
name|r
init|=
operator|new
name|Reference
argument_list|()
decl_stmt|;
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
decl_stmt|;
comment|// Set in = dis so it gets the close below in the finally on our way out.
name|in
operator|=
name|dis
expr_stmt|;
name|r
operator|.
name|readFields
argument_list|(
name|dis
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|FSProtos
operator|.
name|Reference
name|convert
parameter_list|()
block|{
name|FSProtos
operator|.
name|Reference
operator|.
name|Builder
name|builder
init|=
name|FSProtos
operator|.
name|Reference
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setRange
argument_list|(
name|isTopFileRegion
argument_list|(
name|getFileRegion
argument_list|()
argument_list|)
condition|?
name|FSProtos
operator|.
name|Reference
operator|.
name|Range
operator|.
name|TOP
else|:
name|FSProtos
operator|.
name|Reference
operator|.
name|Range
operator|.
name|BOTTOM
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setSplitkey
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|getSplitKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|static
name|Reference
name|convert
parameter_list|(
specifier|final
name|FSProtos
operator|.
name|Reference
name|r
parameter_list|)
block|{
name|Reference
name|result
init|=
operator|new
name|Reference
argument_list|()
decl_stmt|;
name|result
operator|.
name|splitkey
operator|=
name|r
operator|.
name|getSplitkey
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
name|result
operator|.
name|region
operator|=
name|r
operator|.
name|getRange
argument_list|()
operator|==
name|FSProtos
operator|.
name|Reference
operator|.
name|Range
operator|.
name|TOP
condition|?
name|Range
operator|.
name|top
else|:
name|Range
operator|.
name|bottom
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Use this when writing to a stream and you want to use the pb mergeDelimitedFrom    * (w/o the delimiter, pb reads to EOF which may not be what you want).    * @return This instance serialized as a delimited protobuf w/ a magic pb prefix.    * @throws IOException    */
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|convert
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit


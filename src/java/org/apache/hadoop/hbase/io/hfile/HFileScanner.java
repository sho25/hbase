begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/**  * A scanner allows you to position yourself within a HFile and  * scan through it.  It allows you to reposition yourself as well.  *   *<p>A scanner doesn't always have a key/value that it is pointing to  * when it is first created and before  * {@link #seekTo()}/{@link #seekTo(byte[])} are called.  * In this case, {@link #getKey()}/{@link #getValue()} returns null.  At most  * other times, a key and value will be available.  The general pattern is that  * you position the Scanner using the seekTo variants and then getKey and  * getValue.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HFileScanner
block|{
comment|/**    * SeekTo or just before the passed<code>key</code>.  Examine the return    * code to figure whether we found the key or not.    * Consider the key stream of all the keys in the file,     *<code>k[0] .. k[n]</code>, where there are n keys in the file.    * @param key Key to find.    * @return -1, if key< k[0], no position;    * 0, such that k[i] = key and scanner is left in position i; and    * 1, such that k[i]< key, and scanner is left in position i.    * Furthermore, there may be a k[i+1], such that k[i]< key< k[i+1]    * but there may not be a k[i+1], and next() will return false (EOF).    * @throws IOException    */
specifier|public
name|int
name|seekTo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Consider the key stream of all the keys in the file,     *<code>k[0] .. k[n]</code>, where there are n keys in the file.    * @param key Key to find    * @return false if key<= k[0] or true with scanner in position 'i' such    * that: k[i]< key.  Furthermore: there may be a k[i+1], such that    * k[i]< key<= k[i+1] but there may also NOT be a k[i+1], and next() will    * return false (EOF).    */
specifier|public
name|boolean
name|seekBefore
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Positions this scanner at the start of the file.    * @return False if empty file; i.e. a call to next would return false and    * the current key and value are undefined.    * @throws IOException    */
specifier|public
name|boolean
name|seekTo
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Scans to the next entry in the file.    * @return Returns false if you are at the end otherwise true if more in file.    * @throws IOException    */
specifier|public
name|boolean
name|next
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Gets a buffer view to the current key. You must call    * {@link #seekTo(byte[])} before this method.    * @return byte buffer for the key. The limit is set to the key size, and the    * position is 0, the start of the buffer view.    */
specifier|public
name|ByteBuffer
name|getKey
parameter_list|()
function_decl|;
comment|/**    * Gets a buffer view to the current value.  You must call    * {@link #seekTo(byte[])} before this method.    *     * @return byte buffer for the value. The limit is set to the value size, and    * the position is 0, the start of the buffer view.    */
specifier|public
name|ByteBuffer
name|getValue
parameter_list|()
function_decl|;
comment|/**    * Convenience method to get a copy of the key as a string - interpreting the    * bytes as UTF8. You must call {@link #seekTo(byte[])} before this method.    * @return key as a string    */
specifier|public
name|String
name|getKeyString
parameter_list|()
function_decl|;
comment|/**    * Convenience method to get a copy of the value as a string - interpreting    * the bytes as UTF8. You must call {@link #seekTo(byte[])} before this method.    * @return value as a string    */
specifier|public
name|String
name|getValueString
parameter_list|()
function_decl|;
comment|/**    * @return Reader that underlies this Scanner instance.    */
specifier|public
name|HFile
operator|.
name|Reader
name|getReader
parameter_list|()
function_decl|;
comment|/**    * @return True is scanner has had one of the seek calls invoked; i.e.    * {@link #seekBefore(byte[])} or {@link #seekTo()} or {@link #seekTo(byte[])}.    * Otherwise returns false.    */
specifier|public
name|boolean
name|isSeeked
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


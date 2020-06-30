package swaiotos.channel.iot.ss.channel.im;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.session.Session;

/**
 * The type Im message.
 *
 * @ClassName: IMMessage
 * @Author: lu
 * @CreateDate: 2020 /3/16 6:35 PM
 * @Description:
 */
public class IMMessage implements Parcelable {

    public static class Builder {
        private boolean mReply = false;
        private String mId;
        private Session mTarget;
        private Session mSource;
        private String mClientTarget = "";
        private String mClientSource = "";
        private TYPE mType;
        private String mContent = "";
        private boolean mBroadcast = false;
        private Map<String, String> mExtra = new HashMap<>();

        public Builder() {

        }

        public Builder(IMMessage message) {
            this(message, false);
        }

        public Builder(IMMessage message, boolean reply) {
            this.mReply = reply;
            if (!reply) {
                this.mTarget = message.mTarget;
                this.mSource = message.mSource;
                this.mClientTarget = message.mClientTarget;
                this.mClientSource = message.mClientSource;
            } else {
                this.mTarget = message.mSource;
                this.mSource = message.mTarget;
                this.mClientTarget = message.mClientSource;
                this.mClientSource = message.mClientTarget;
                this.mId = message.mId;
            }
            this.mType = TYPE.valueOf(message.mType);
            this.mContent = message.mContent;
            this.mExtra.putAll(message.mExtra);
        }

        public Builder setBroadcast(boolean broadcast) {
            mBroadcast = broadcast;
            return this;
        }

        public Builder setTarget(Session target) {
            this.mTarget = target;
            return this;
        }

        public Builder setSource(Session mSource) {
            this.mSource = mSource;
            return this;
        }

        public Builder setClientTarget(String mClientTarget) {
            this.mClientTarget = mClientTarget;
            return this;
        }

        public Builder setClientSource(String mClientSource) {
            this.mClientSource = mClientSource;
            return this;
        }

        public Builder setType(TYPE mType) {
            this.mType = mType;
            return this;
        }

        public Builder setContent(String mContent) {
            this.mContent = mContent;
            return this;
        }

        public Builder putExtra(String key, String value) {
            this.mExtra.put(key, value);
            return this;
        }

        public IMMessage build() {
            if (this.mReply) {
                return new IMMessage(mId, mSource, mBroadcast ? Session.BROADCAST : mTarget, mClientTarget, mClientSource, mType, mContent, mExtra);
            } else {
                return new IMMessage(mSource, mBroadcast ? Session.BROADCAST : mTarget, mClientTarget, mClientSource, mType, mContent, mExtra);
            }
        }


        public static IMMessage createCtrMessage(Session source, Session target, String text) {
            return new Builder()
                    .setSource(source)
                    .setTarget(target)
                    .setContent(text)
                    .setType(TYPE.CTR)
                    .putExtra(SSChannel.FORCE_SSE, "true")
                    .build();
        }

        public static IMMessage replyCtrMessage(IMMessage message, String text) {
            return new Builder(message, true)
                    .setType(TYPE.CTR)
                    .setContent(text)
                    .putExtra(SSChannel.FORCE_SSE, "true")
                    .build();
        }

        public static IMMessage createTextMessage(Session source, Session target, String sourceClient, String targetClient, String text) {
            return new Builder()
                    .setTarget(target)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(text)
                    .setType(TYPE.TEXT)
                    .build();
        }

        public static IMMessage createBroadcastTextMessage(Session source, String sourceClient, String targetClient, String text) {
            return new Builder()
                    .setBroadcast(true)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(text)
                    .setType(TYPE.TEXT)
                    .build();
        }

        public static IMMessage replyTextMessage(IMMessage message, String text) {
            return new Builder(message, true)
                    .setType(TYPE.TEXT)
                    .setContent(text)
                    .build();
        }

        public static IMMessage createImageMessage(Session source, Session target, String sourceClient, String targetClient, File content) {
            return new Builder()
                    .setTarget(target)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(content.getAbsolutePath())
                    .setType(TYPE.IMAGE)
                    .build();
        }

        public static IMMessage createBroadcastImageMessage(Session source, String sourceClient, String targetClient, File content) {
            return new Builder()
                    .setBroadcast(true)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(content.getAbsolutePath())
                    .setType(TYPE.IMAGE)
                    .build();
        }

        public static IMMessage createAudioMessage(Session source, Session target, String sourceClient, String targetClient, File content) {
            return new Builder()
                    .setTarget(target)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(content.getAbsolutePath())
                    .setType(TYPE.AUDIO)
                    .build();
        }

        public static IMMessage createBroadcastAudioMessage(Session source, String sourceClient, String targetClient, File content) {
            return new Builder()
                    .setBroadcast(true)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(content.getAbsolutePath())
                    .setType(TYPE.AUDIO)
                    .build();
        }

        public static IMMessage createVideoMessage(Session source, Session target, String sourceClient, String targetClient, File content) {
            return new Builder()
                    .setTarget(target)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(content.getAbsolutePath())
                    .setType(TYPE.VIDEO)
                    .build();
        }

        public static IMMessage createBroadcastVideoMessage(Session source, String sourceClient, String targetClient, File content) {
            return new Builder()
                    .setBroadcast(true)
                    .setSource(source)
                    .setClientTarget(targetClient)
                    .setClientSource(sourceClient)
                    .setContent(content.getAbsolutePath())
                    .setType(TYPE.VIDEO)
                    .build();
        }

        public static IMMessage modifyContent(IMMessage ori, String content) {
            return new Builder(ori)
                    .setContent(content)
                    .build();
        }

        public static IMMessage decode(String in) throws Exception {
            return new IMMessage(in);
        }
    }


    public enum TYPE {
        TEXT,
        IMAGE,
        AUDIO,
        VIDEO,
        CTR,
    }

    /**
     * 每条message的唯一id，自动生成子UUID
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     */
    private final String mId;
    /**
     * 智屏体系下的消息接收方session
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     * @see Session
     */
    private final Session mTarget;
    /**
     * 智屏体系下的消息发送方session
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     * @see Session
     */
    private final Session mSource;

    /**
     * 消息接收方中需要处理此条消息的clientID
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     * @see swaiotos.channel.iot.ss.SSChannelClient
     */
    private final String mClientTarget;
    /**
     * 消息发送方中发出此条消息的clientID
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     */
    private final String mClientSource;
    /**
     * 消息类型
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     * @see TYPE
     */
    private final String mType;
    /**
     * 消息体
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     */
    private final String mContent;
    /**
     * 可选的附加参数表
     *
     * @param in the in
     * @return the im message
     * @throws Exception the exception
     */
    private final Map<String, String> mExtra;

    private boolean mReply = false;

    IMMessage(String in) throws Exception {
        JSONObject object = new JSONObject(in);
        this.mId = object.getString("id");
        this.mSource = Session.Builder.decode(object.getString("source"));
        this.mTarget = Session.Builder.decode(object.getString("target"));
        this.mClientTarget = object.getString("client-target");
        this.mClientSource = object.getString("client-source");
        this.mType = object.getString("type");
        this.mContent = object.getString("content");
        this.mExtra = new HashMap<>();
        JSONObject extra = object.getJSONObject("extra");
        Iterator<String> keys = extra.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            this.mExtra.put(key, extra.getString(key));
        }
        this.mReply = object.optBoolean("reply");
    }

    IMMessage(Parcel in) {
        ClassLoader classLoader = getClass().getClassLoader();
        mId = in.readString();
        mSource = in.readParcelable(classLoader);
        mTarget = in.readParcelable(classLoader);
        mClientTarget = in.readString();
        mClientSource = in.readString();
        mType = in.readString();
        mContent = in.readString();
        this.mExtra = in.readHashMap(classLoader);
        this.mReply = Boolean.valueOf(in.readString());
    }

    IMMessage(String id, Session source, Session target, String clientTarget, String clientSource, TYPE type, String content, Map<String, String> extras) {
        this.mId = id;
        this.mSource = source;
        this.mTarget = target;
        this.mClientTarget = clientTarget;
        this.mClientSource = clientSource;
        this.mType = type.name();
        this.mContent = content;
        this.mExtra = new HashMap<>(extras);
    }

    IMMessage(Session source, Session target, String clientTarget, String clientSource, TYPE type, String content, Map<String, String> extras) {
        this(UUID.randomUUID().toString(), source, target, clientTarget, clientSource, type, content, extras);
    }

    public final String getId() {
        return mId;
    }

    public final Session getSource() {
        return mSource;
    }

    public final String getClientSource() {
        return mClientSource;
    }

    public final String getClientTarget() {
        return mClientTarget;
    }

    public final boolean isBroadcastMessage() {
        return mTarget.isBroadcast();
    }

    public final Session getTarget() {
        return mTarget;
    }

    public final TYPE getType() {
        return TYPE.valueOf(mType);
    }

    public final void putExtra(String key, String value) {
        synchronized (mExtra) {
            mExtra.put(key, value);
        }
    }

    public final String getExtra(String key) {
        synchronized (mExtra) {
            return mExtra.get(key);
        }
    }

    public final String getContent() {
        return mContent;
    }

    public final String encode() {
        JSONObject object = new JSONObject();
        try {
            object = object.put("id", this.mId);
            object = object.put("source", this.mSource.toString());
            object = object.put("target", this.mTarget.toString());
            object = object.put("client-source", this.mClientSource);
            object = object.put("client-target", this.mClientTarget);
            object = object.put("type", this.mType);
            object = object.put("content", this.mContent);
            object = object.put("extra", new JSONObject(mExtra));
            object = object.put("reply", this.mReply);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    public String toString() {
        return encode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setReply(boolean reply) {
        mReply = reply;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeParcelable(mSource, flags);
        dest.writeParcelable(mTarget, flags);
        dest.writeString(mClientTarget);
        dest.writeString(mClientSource);
        dest.writeString(mType);
        dest.writeString(mContent);
        dest.writeMap(mExtra);
        dest.writeString(Boolean.toString(mReply));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IMMessage message = (IMMessage) o;
        return mId.equals(message.mId);
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    public static final Creator<IMMessage> CREATOR = new Creator<IMMessage>() {
        @Override
        public IMMessage createFromParcel(Parcel source) {
            return new IMMessage(source);
        }

        @Override
        public IMMessage[] newArray(int size) {
            return new IMMessage[size];
        }
    };
}
